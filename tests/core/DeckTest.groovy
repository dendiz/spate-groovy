package core;

import groovy.util.GroovyTestCase;

class DeckTest extends GroovyTestCase {

	public void testDeck() {
		def d = new Deck()
		assert d.cards.size() == 52
		assert "Ah" in d.cards
		assert "Kc" in d.cards
	}

	public void testShuffle() {
		def d = new Deck()
		def d2 = new Deck()
		def same = 0
		d.shuffle()
		d.cards.eachWithIndex {dc,i->
			if (d.cards[i] == d2.cards[2]) same ++
		}
		assert same < 26
	}

	public void testDeal() {
		def d = new Deck()
		52.times { d.deal() }
		assert d.cards.size() == 0
	}
	
	public void testDeal2() {
		def d = new Deck()
		def dr = d.cards.reverse()
		dr.each { assert it == d.deal() }
	}
	
	public void testExtract() {
		def d = new Deck()
		d.extract(["Ah","As", "Ac"])
		assert "Ah" in d.cards == false
		assert "As" in d.cards == false
		assert "Ac" in d.cards == false
	}

}