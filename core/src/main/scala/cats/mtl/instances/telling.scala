package cats
package mtl
package instances

import cats.data.WriterT
import cats.mtl.applicative.Telling

trait TellingInstances extends TellingInstancesLowPriority {
  implicit final def tellIndT[Inner[_], Outer[_[_], _], L]
  (implicit lift: monad.Trans.Aux[CurryT[Outer, Inner]#l, Inner, Outer],
   under: Telling[Inner, L]): Telling[CurryT[Outer, Inner]#l, L] = {
    tellInd[CurryT[Outer, Inner]#l, Inner, L](lift, under)
  }
}

private[instances] trait TellingInstancesLowPriority extends TellingInstancesLowPriority1 {
  implicit final def tellInd[M[_], Inner[_], L](implicit
                                                lift: monad.Layer[M, Inner],
                                                under: Telling[Inner, L]
                                               ): Telling[M, L] = {
    new Telling[M, L] {
      val applicative: Applicative[M] = lift.outerInstance
      val monoid: Monoid[L] = under.monoid

      def tell(l: L): M[Unit] = lift.layer(under.tell(l))

      def writer[A](a: A, l: L): M[A] = lift.layer(under.writer(a, l))

      def tuple[A](ta: (A, L)): M[A] = lift.layer(under.tuple(ta))
    }
  }
}

private[instances] trait TellingInstancesLowPriority1 {
  implicit final def tellWriter[M[_], L](implicit L: Monoid[L], M: Monad[M]): Telling[CurryT[WriterTCL[L]#l, M]#l, L] = {
    new Telling[CurryT[WriterTCL[L]#l, M]#l, L] {
      val applicative = WriterT.catsDataMonadWriterForWriterT(M, L)
      val monoid: Monoid[L] = L

      def tell(l: L): WriterT[M, L, Unit] = WriterT.tell(l)

      def writer[A](a: A, l: L): WriterT[M, L, A] = WriterT.put(a)(l)

      def tuple[A](ta: (A, L)): WriterT[M, L, A] = WriterT(M.pure(ta.swap))
    }
  }

  implicit final def tellTuple[L](implicit L: Monoid[L]): Telling[TupleC[L]#l, L] = {
    new Telling[TupleC[L]#l, L] {
      val applicative = cats.instances.tuple.catsStdMonadForTuple2
      val monoid: Monoid[L] = L

      def tell(l: L): (L, Unit) = (l, ())

      def writer[A](a: A, l: L): (L, A) = (l, a)

      def tuple[A](ta: (A, L)): (L, A) = ta.swap
    }
  }
}

object telling extends TellingInstances
