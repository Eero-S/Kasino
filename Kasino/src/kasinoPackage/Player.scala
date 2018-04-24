package kasinoPackage
import scala.collection.mutable.Buffer
import scala.io.StdIn._

// Represents the player objects in the game.
class Player(var hand: Buffer[Card], val board: Board, val name: String) {

  require((hand.size <= 4 && hand.size >= 0), "Wrong hand size")

  override def toString = name + " " + hand.mkString(", ")
  def isBot = false
  def play: Unit = println("not a bot") 

  // The cards a player has collected during each round.
  val collected: Buffer[Card] = Buffer()
  // Points a player has collected in the game.
  var points = 0

  // Methods for counting points.
  def calcPointCards = collected.map(_.points).sum
  def calcSpades = collected.filter(_.suit == "Spades").length
  def calcLength = collected.size
  def winSpades = points += 2
  def winSize = points += 1
  def addPointCards = points += calcPointCards

  // Helper method that tells what kind of sets can be taken from the board with a certain card.
  def inspectSets(target: Card): Option[Seq[Seq[Card]]] = {
    val board = this.board.cards
    val targ = target.valueHand
    val sopivat = board.filter(_.valueTable <= targ).toSet
    val cards = sopivat.subsets().toList.map(_.toSeq).filter(_.map(_.valueTable).sum == targ).distinct
    if (cards.isEmpty) None
    else Option(cards)
  }

  /* Checks recursively if it is legal to take the candidate cards.
   * @param acandidates		Candidate cards that are smaller than the target.
   * @param targ					The hand value of the target card.
   */
  def legalityHelper(candidates: Seq[Card], targ: Int): Boolean = {
    require(candidates.forall(_.valueTable < targ), "Cards are too big")
    val subs = candidates.toSet.subsets().toList.map(_.toSeq).filter(_.map(_.valueTable).sum == targ).distinct // subsets that equal the target.
    val allThere = candidates.forall(subs.flatten.contains(_)) // Checks if every candidate is being used in the subsets.

    if (candidates.isEmpty) { // If a is empty, all possible subsets have been tested.
      true
    } else if (!allThere) {
      false
    } else {
      /* Removes every subset one by one, and recursively test if there are any combinations where it is legal to play these cards.
      *  Only one legal combination is needed to prove that it is legal to take candidates. */
      var any = false
      for (i <- subs.indices) {
        val newSet = candidates.toSet -- subs(i)
        val b = legalityHelper(newSet.toSeq, targ)
        if (b) any = true
      }
      any
    }
  }

  /* Checks if it is 'legal' to make the play.
   * @param card				The card to be played.
   * @param candidates 	The cards to be taken.
   */
  def legality(target: Card, candidates: Seq[Card]): Boolean = {
    val smaller = candidates.filter(_.valueTable < target.valueHand) //  Cards that are smaller than target card.
    val subs = smaller.toSet.subsets().toList.map(_.toSeq).filter(_.map(_.valueTable).sum == target.valueHand).distinct // subsets that equal the target.

    if (candidates.isEmpty) {
      true
    } else if (inspectSets(target).isEmpty) { // If there are no subsets that equal target cards value, there is no legal plays.
      false
    } else if (candidates.exists(_.valueTable > target.valueHand)) { // Can not take bigger cards.
      false
    } else if (smaller.isEmpty) { // if smaller is empty, then all candidate cards must equal the target card (there are no bigger nor smaller cards).
      true
    } else if (!smaller.forall(subs.flatten.contains(_)) || subs.isEmpty) { // Check if subs contains each card from smaller.
      false
    } else {
      legalityHelper(smaller, target.valueHand)
    }
  }

  /* The method that lets players choose what to play, and what to take from the board.
   * If the play is legal, completes the play and returns true.
   */
  def chooseAction(fromHand: Card, fromTable: Buffer[Card]): Boolean = {
    var r = true
    if (this.legality(fromHand, fromTable)) {  
      
      this.removeFromHand(fromHand)
      
      if (!fromTable.isEmpty) {
        val cardsToBeTaken: Buffer[Card] = fromTable ++ Buffer(fromHand)
        this.collect(cardsToBeTaken)

        // Mökki
        if (this.board.cards.isEmpty) {
          points += 1
        }
        
      } else {
        board.add(fromHand)  // If not able to take any cards, put card to table.
      }
    } else {
      r = false
    }
    r
  }
  
  def resetAll(): Unit = {
    resetRound()
    this.points = 0
  }

  def resetRound(): Unit = {
    this.collected.clear()
    this.hand.clear()    
  }

  def collect(cards: Seq[Card]): Unit = {
    collected ++= cards
    board.take(cards)
  }

  private def removeFromHand(card: Card): Unit = {
    hand -= card
  }

}