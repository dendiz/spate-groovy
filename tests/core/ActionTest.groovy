package core;

import groovy.util.GroovyTestCase;

class ActionTest extends GroovyTestCase {
	def gi
	public void setUp() {
		gi = [
			"get_to_call": {return 5}
			,"current_player": 1
			,"get_bb_size": {return 2}
			,"get_min_raise": {return 2}
		]	
	}
	public void testBigblind() {
		def a = Action.bigblind(5)
		assert a.tocall == 0
		assert a.amount == 5
	}

	public void testSmallblind() {
		def ac = Action.smallblind(5)
		assert ac.tocall == 0
		assert ac.amount == 5
	}

	public void testFold() {
		def a = Action.fold(gi)
		assert a.tocall == 5
		assert a.amount == 0
	}

	public void testCheck() {
		def a = Action.check()
		assert a.tocall == 0
		assert a.amount == 0
		assert a.type == Action.CHECK
	}

	public void testCheck_or_fold() {
		def a = Action.check_or_fold(gi)
		assert a.type == Action.FOLD
	}
	
	public void testCheck_or_fold2() {
		gi.get_to_call = { return 0 }
		def a = Action.check_or_fold(gi)
		assert a.type == Action.CHECK
	}

	public void testCall() {
		def a = Action.call(gi)
		assert a.tocall == 5
		assert a.type == Action.CALL
		assert a.amount == 0
	}
	
	public void testCall2() {
		gi.get_to_call = {return 0}
		def a = Action.call(gi)
		assert a.type == Action.CHECK
		assert a.tocall == 0
		assert a.amount == 0
		
	}
	public void testWaiting() {
		def a = Action.waiting()
		assert a.tocall == 0
		assert a.type == Action.WAITING
		assert a.amount == 0
	}

	public void testBet() {
		def a = Action.bet(gi)
		assert a.type == Action.BET
		assert a.tocall == 0
		assert a.amount == 2
	}

	public void testRaise() {
		gi.get_min_raise = {return 2}
		def a = Action.raise(gi)
		assert a.type == Action.RAISE
		assert a.tocall == 5
		assert a.amount == 2
		
	}
	
	public void testRaise2() {
		gi.get_min_raise = { return 0 }
		def a = Action.raise(gi)
		assert a.type == Action.CALL
		assert a.amount == 0
		assert a.tocall == 5
	}
	
	public void testRaise3() {
		gi.get_to_call = {return 0}
		gi.get_min_raise = {return 2}
		gi.get_bb_size = {return 2}
		def a = Action.raise(gi)
		assert a.type == Action.BET
		assert a.tocall == 0
		assert a.amount == 2
	}
}