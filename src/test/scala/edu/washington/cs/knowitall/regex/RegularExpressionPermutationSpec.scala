package edu.washington.cs.knowitall.regex
import org.junit.runner.RunWith
import edu.washington.cs.knowitall.regex.Expression.BaseExpression
import scala.collection.JavaConversions._
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import scala.collection.immutable.SortedSet

@RunWith(classOf[JUnitRunner])
class RegularExpressionPermutationSpec extends Specification {
  val tokens = List("<this>+", "<is>*", "<a>?", "<test>")
  tokens.permutations.foreach { permutation =>
    permutation.mkString("'", " ", "'") should {
      val regex = RegularExpressions.word(permutation.mkString(" "))
      testRegex(regex)
      
      // TODO: hack
      1 must_==(1)
    }
  }
  
  def testRegex(regex: RegularExpression[String]) = {
    case class TestCase(tokens: List[String], value: Boolean) extends Ordered[TestCase] {
      def extend(test: TestCase) =
        TestCase(tokens ::: test.tokens, value & test.value)
      
      def compare(that: TestCase) = {
        val c1 = this.tokens.mkString(" ") compare that.tokens.mkString(" ")
        if (c1 != 0) c1
        else this.value.compare(that.value)
      }
    }
    
    def makeCases(exprs: List[Expression[String]]) = {
      def makeNext(expr: Expression[String]): (List[List[String]], List[List[String]]) = expr match {
        case star: Expression.Star[_] =>
          val source = star.expr.asInstanceOf[BaseExpression[String]].source
          (List(), List(List(), List(source), List(source, source)))
        case plus: Expression.Plus[_] =>
          val source = plus.expr.asInstanceOf[BaseExpression[String]].source
          (List(List()), List(List(source), List(source, source)))
        case option: Expression.Option[_] =>
          val source = option.expr.asInstanceOf[BaseExpression[String]].source
          (List(List(source, source)), List(List(), List(source)))
        case base: Expression.BaseExpression[_] =>
          val source = base.source
          (List(List(), List(source, source)), List(List(source)))
        case _ => (List(), List())
      }
      
      def makeNextCase(expr: Expression[String]) = {
        val (falses, trues) = makeNext(expr)
        falses.map(TestCase(_, false)) ::: trues.map(TestCase(_, true))
      }
    
      def combine(tests: List[TestCase], nexts: List[TestCase]) = 
        if (nexts.isEmpty) tests
        else for (test <- tests; next <- nexts) yield (test extend next)
        
      def rec(exprs: List[Expression[String]]): List[TestCase] = exprs match {
        case expr :: exprs =>
          val tests = makeNextCase(expr)
          val extentions = rec(exprs)
          combine(tests, extentions)
        case Nil => List()
      }
      
      SortedSet[TestCase]() ++ rec(exprs)
    }
    
    makeCases(regex.expressions.toList).foreach { test =>
      ((if (test.value) "" else "not") + " match " + test.tokens.mkString("'", " ", "'")) in {
        regex.matches(test.tokens) must beTrue.iff(test.value)
      }
    }
  }
}