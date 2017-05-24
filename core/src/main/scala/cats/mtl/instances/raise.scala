package cats
package mtl
package instances

import cats.data.EitherT

trait RaisingInstances extends RaisingLowPriorityInstances {
  implicit def raiseNIndT[T[_[_], _], M[_], E]
  (implicit lift: MonadTrans.AuxIO[CurryT[T, M]#l, M, T],
   under: Raising[M, E]): Raising[CurryT[T, M]#l, E] =
    raiseNInd[CurryT[T, M]#l, M, E](lift, under)
}

trait RaisingLowPriorityInstances extends RaisingLowPriorityInstances1 {
  implicit def raiseNInd[M[_], Inner[_], E](implicit
                                            lift: MonadLayer[M, Inner],
                                            under: Raising[Inner, E]
                                           ): Raising[M, E] =
    new Raising[M, E] {
      def raiseError[A](e: E): M[A] =
        lift.layer(under.raiseError(e))
    }
}


trait RaisingLowPriorityInstances1 {
  implicit def raiseNEither[M[_], E](implicit M: Applicative[M]): Raising[EitherTC[M, E]#l, E] =
    new Raising[EitherTC[M, E]#l, E] {
      def raiseError[A](e: E): EitherT[M, E, A] =
        EitherT(M.pure(Left(e)))
    }

}


object raise extends RaisingInstances
