package core;

class Deck {
	def cards = []
	public Deck() {
		reset()
	}
	
	def reset() {
		cards = []
		[2,3,4,5,6,7,8,9,"T","J","Q","K","A"].each { r->
			["c","h","s","d"].each { s-> cards << r+s }
		}
		this
	}
	def shuffle() {
		def rand1 = new Random()
		300.times {
			def r1 = rand1.nextInt(52)
			cards << cards.remove(r1)
		}
		this
	}
	
	def deal() {
		return cards.pop()
	}
	
	def extract(hand) {
		assert hand instanceof ArrayList, "need to provide an array to extract."
		hand.each { c-> cards.remove(c) }
		this
	}
	
}