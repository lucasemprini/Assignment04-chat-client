import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import model.actors.RestClient
import model.messages.{RestObject, User, UserMsg}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class RestTester() extends TestKit(ActorSystem("MySystem")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  var client: ActorRef = system.actorOf(RestClient.props())

  var probe = TestProbe()
  client tell (UserMsg("jacopo47"), probe.ref)

  val user: User = probe.expectMsgType[User](50000 millis)

  assert(user.getName().equals("jacopo"))
}
