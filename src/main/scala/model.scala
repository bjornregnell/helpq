case class User(name: String, number: Int){
  require(name == User.validName(name), s"invalid user name: $name")
  require(number > 0, s"invalid user number: $number")
  val id = s"$name-$number"
  override def toString = id
}

object User {
  val DefaultUserName = "oddput"
  val MaxNameLength = 25
 
  def validName(s: String): String = 
    if (s.nonEmpty) s.filter(_.isLetter).take(MaxNameLength).toLowerCase 
    else DefaultUserName
  
  def validUserId(s: String): String = 
    s.filter(c => c.isLetterOrDigit || c == '-').toLowerCase

  def fromUserId(uid: String): Option[User] = scala.util.Try {
    val xs = validUserId(uid).split('-')
    assert(xs.length == 2)
    User(validName(xs(0)),xs(1).toInt)
  }.toOption
}

case class RoomKey private (course: String, roomName: String){
  override def toString = s"<br>&nbsp;RoomKey($course,$roomName)"
}
object RoomKey {
  val MaxCourseLength = 25
  val   MaxRoomLength = 20
  val   DefaultCourse = "KURS01"
  val     DefaultRoom = "Shäraton"

  def validCourse(s: String): String = 
    if (s.nonEmpty) s.filter(c => c.isLetterOrDigit).take(MaxCourseLength).toUpperCase 
    else DefaultCourse
  
  def validRoomName(s: String): String = 
    if (s.nonEmpty) s.filter(c => c.isLetterOrDigit).take(MaxRoomLength).toLowerCase.capitalize 
    else DefaultRoom
  
  def apply(course: String, roomName: String): RoomKey = 
    new RoomKey(validCourse(course), validRoomName(roomName))
}

case class SessionBlock(start: Int, end: Int)
object SessionBlock {
  val sessionBlocks = Vector(SessionBlock(8, 10), SessionBlock(10, 12), SessionBlock(13, 15), SessionBlock(15, 17))
}

object Room {
  val HoursUntilExpired = 3
}
case class Room(
  course: String, 
  name: String, 
  supervisor: Option[User] = None,
  students: Set[User] = Set(), 
  helpQueue: Vector[User] = Vector(), 
  approvalQueue: Vector[User] = Vector(), 
  created: Date = Date.now,
){
  def wantHelp(u: User): Room = copy(
    helpQueue = if (helpQueue.contains(u)) helpQueue else helpQueue :+ u,
    approvalQueue = approvalQueue.filterNot(_ == u)
  )

  def wantApproval(u: User): Room = copy(
    helpQueue = helpQueue.filterNot(_ == u),
    approvalQueue = if (approvalQueue.contains(u)) approvalQueue else approvalQueue :+ u
  )

  def working(u: User): Room = copy(
    students = students + u,
    helpQueue = helpQueue.filterNot(_ == u),
    approvalQueue = approvalQueue.filterNot(_ == u)
  )

  def goodbye(u: User): Room = copy(
    students = students - u,
    helpQueue = helpQueue.filterNot(_ == u),
    approvalQueue = approvalQueue.filterNot(_ == u),
    supervisor = supervisor.flatMap(s => if (s == u) None else Some(s))
  )

  def clearHelpQueue(): Room = copy(helpQueue = Vector())

  def clearApprovalQueue(): Room = copy(approvalQueue = Vector())

  def popHelpQueue(): Room = copy(helpQueue = helpQueue.drop(1))

  def popApprovalQueue(): Room = copy(approvalQueue = approvalQueue.drop(1))

  def isExpired(): Boolean = {
    val expiredByMaximumTime = created < Date.now.minusHours(Room.HoursUntilExpired)
    val sessionBlock = SessionBlock.sessionBlocks.find((block) => {
      block.start >= created.hour && block.end <= created.hour
    })
    if(sessionBlock.isEmpty) {
      expiredByMaximumTime
    }
    else {
      expiredByMaximumTime || Date.now.hour >= sessionBlock.get.end
    }
  }

  def isActive: Boolean = supervisor.isDefined || students.nonEmpty

  def isRemovable: Boolean = isExpired || !isActive

  override def toString = 
    s"Room($course, $name, supervisor=$supervisor, students=$students), helpQueue=$helpQueue, approvalQueue=$approvalQueue, created=${created})"
}