import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import model.actors.RestClient
import model.messages._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class RestTester() extends TestKit(ActorSystem("MySystem")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val user = new User("jacopo47", "jacopin")
  user.queryParams
  var client: ActorRef = system.actorOf(RestClient.props())

  var probe = TestProbe()
  /*client tell (UserMsg("jacopo47"), probe.ref)

  val user: User = probe.expectMsgType[User](50000 millis)

  assert(user.getName.equals("jacopo"))*/

  /*client tell (GetChatMsg("1"), probe.ref)

  val chat: Chat = probe.expectMsgType[ChatMsgRes](50000 millis).chat*/


  val chat: Chat = new Chat("1", "Fantacalcio", null)
  client tell (SetChatMsg(chat), probe.ref)

  probe.expectMsgType[OkSetChatMsg](50000 millis)
}
