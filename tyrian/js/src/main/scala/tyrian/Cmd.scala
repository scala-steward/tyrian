package tyrian

import cats.effect.kernel.Concurrent

import scala.annotation.targetName

/** A command describes some side-effect to perform.
  */
sealed trait Cmd[+F[_], +Msg]:
  /** Transforms the type of messages produced by the command */
  def map[OtherMsg](f: Msg => OtherMsg): Cmd[F, OtherMsg]

object Cmd:
  given CanEqual[Cmd[_, _], Cmd[_, _]] = CanEqual.derived

  /** The empty command represents the absence of any command to perform */
  case object Empty extends Cmd[Nothing, Nothing]:
    def map[OtherMsg](f: Nothing => OtherMsg): Empty.type = this

  /** Runs a task that produces no message */
  final case class SideEffect[F[_]](task: F[Unit]) extends Cmd[F, Nothing]:
    def map[OtherMsg](f: Nothing => OtherMsg): SideEffect[F] = this
  object SideEffect:
    def apply[F[_]: Concurrent](thunk: => Unit): SideEffect[F] =
      SideEffect(Concurrent[F].pure(thunk))

  /** Simply produces a message that will then be actioned. */
  final case class Emit[Msg](msg: Msg) extends Cmd[Nothing, Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Emit[OtherMsg] = Emit(f(msg))

  /** Represents runnable concurrent task that produces a message */
  final case class Run[F[_], A, Msg](
      task: F[A],
      toMsg: A => Msg
  ) extends Cmd[F, Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Run[F, A, OtherMsg] = Run(task, toMsg andThen f)
  object Run:
    @targetName("Cmd.Run separate param lists")
    def apply[F[_], A, Msg](task: F[A])(toMessage: A => Msg): Run[F, A, Msg] =
      Run(task, toMessage)

  /** Merge two commands into a single one */
  case class Combine[F[_], Msg](cmd1: Cmd[F, Msg], cmd2: Cmd[F, Msg]) extends Cmd[F, Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Combine[F, OtherMsg] = Combine(cmd1.map(f), cmd2.map(f))

  final def combine[F[_], Msg, LubMsg >: Msg](a: Cmd[F, Msg], b: Cmd[F, LubMsg]): Cmd[F, LubMsg] =
    (a, b) match {
      case (Cmd.Empty, Cmd.Empty) => Cmd.Empty
      case (Cmd.Empty, c2)          => c2
      case (c1, Cmd.Empty)          => c1
      case (c1, c2)                   => Cmd.Combine[F, LubMsg](c1, c2)
    }

  /** Treat many commands as one */
  case class Batch[F[_], Msg](cmds: List[Cmd[F, Msg]]) extends Cmd[F, Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Batch[F, OtherMsg] = this.copy(cmds = cmds.map(_.map(f)))
  object Batch:
    def apply[F[_], Msg](cmds: Cmd[F, Msg]*): Batch[F, Msg] =
      Batch(cmds.toList)
