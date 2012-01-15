package core;

import groovy.util.GroovyTestCase;

class BotpitTest extends GroovyTestCase {
	def gi,b,conf
	public void setUp() {
		gi = [
		  "get_to_call": {return 5}
		  , "current_player" : 1
		  , "bigblind_size": 2
		  , "get_min_raise": {return 2}
		]
		b = new Botpit()
		b.load_players()
		conf = new ConfigSlurper().parse(new File("botpit.properties").toURL())
	}
	public void testNew_hand() {
		assert b.deck.cards.size() == 52
		b.actions.eachWithIndex {k,v-> assert v.type == Action.WAITING}
		b.bets.each {k,v -> assert v == 0 }
		assert b.board == []
		assert b.raises == 0
	}

	public void testIs_noshowdown() {
		b.players[1..b.players.size()-1].each { b.actions[it.name] = Action.fold(gi) }
		assert b.is_noshowdown() == true
		b.players.each { b.actions[it.name] = Action.call(gi) }
		assert b.is_noshowdown() == false
	}

	public void testSet_waiting() {
		b.actions = ["p1":Action.fold(gi), "p2": Action.call(gi)]
		b.set_waiting()
		assert b.actions["p1"].type == Action.FOLD
		assert b.actions["p2"].type == Action.WAITING
	}

	public void testGet_num_active() {
		b.players.each { b.actions[it.name] = Action.fold(gi) }
		assert b.get_num_active() == 0
		b.players.each { b.actions[it.name] = Action.call(gi) }
		assert b.get_num_active() == b.players.size()
	}

	public void testPreflop() {
		b.new_hand(3)
		b.preflop()
		def exp = conf.stack * b.players.size()
		assert b.stacks.values().inject(0) {x,y-> x+y } < exp
		assert b.pot > 0
		println b.stacks
		println b.bets
		println b.pot
		
		assert b.bets.values().inject(0) {x,y ->x+y} > 0
	}

	public void testPostFlop() {
		b.new_hand(3)
		b.preflop()
		b.flop()
		b.turn()
		b.river()
		
	}
	public void testIs_betting_over() {
		b.actions[b.players[0].name] = Action.call(gi)
		(1..b.players.size()-1).each { b.actions[b.players[it].name] = Action.fold(gi)}
		assert b.is_betting_over() == true
		
		b.actions[b.players[0].name] = Action.check(gi)
		(1..b.players.size()-1).each { b.actions[b.players[it].name] = Action.check(gi)}
		assert b.is_betting_over() == true
		
		b.actions[b.players[0].name] = Action.raise(gi)
		b.bets[b.players[0].name] = 5
		(1..b.players.size()-1).each { 
			b.actions[b.players[it].name] = Action.call(gi)
			b.bets[b.players[it].name] = 5
		}
		assert b.is_betting_over() == true
		
		b.actions[b.players[0].name] = Action.raise(gi)
		b.bets[b.players[0].name] = 10
		(1..b.players.size()-1).each { 
			b.actions[b.players[it].name] = Action.call(gi)
			b.bets[b.players[it].name] = 5
		}
		assert b.is_betting_over() == false
				
	}

	public void testGet_to_call() {
		b.new_hand()
		b.players.each { b.bets[it.name]=10 }
		b.bets[b.players[0].name] = 5
		assert b.get_to_call(0) == 5
	}

	public void testGet_min_raise() {
		b.raises = 0
		assert b.get_min_raise(0) == conf.bigblind
		b.raises = 4
		assert b.get_min_raise(0) == 0
	}

	public void testInterprete_action() {
		b.new_hand(0)
		b.interprete_action Action.call(gi)
		assert b.pot == 5
		
		b.new_hand(1)
		b.interprete_action	Action.raise(gi)
		assert b.pot == 7
		
		b.new_hand(2)
		b.interprete_action Action.fold(gi)
		assert b.pot == 0
		
	}

	public void testLoad_players() {
		assert b.players.size() == conf.numplayers
		b.stacks.each {k,v -> assert v == conf.stack }
	}

	public void testSteal_pot() {
		b.stacks = ["p1":0, "p2":0]
		b.actions = ["p1":Action.fold(gi), "p2": Action.call(gi)]
		b.pot = 5
		b.steal_pot()
		assert b.stacks["p1"] == 0
		assert b.stacks["p2"] == 5
	}
}