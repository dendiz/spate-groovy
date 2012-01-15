package core;

import Action

import spadeseval.evaluator.SpadesEval;
import util.Log;
import bot.Callbot;
class Botpit {
	
	static PREFLOP = 0
	static FLOP = 1
	static TURN = 2
	static RIVER = 3
	static SHOWDOWN = 4
	def pot=0,raises=0,players=[],actions=[:],stacks=[:],bets=[:];
	def game_stage = -1, button = -1, current_player = -1
	def board = [];
	def deck, conf;
	
	public Botpit() {
		deck = new Deck()
		conf = new ConfigSlurper().parse(new File("botpit.properties").toURL())
		println conf
	}
	
	def run() {
		load_players()
		conf.numsimrounds.times {
			if ((int) (100 * it / conf.numsimrounds) % ((int)(conf.numsimrounds / 10)) == 0) print "."
			new_hand(it)
			preflop() || flop() || turn() || river() || showdown()
			if (game_stage != SHOWDOWN) steal_pot()
		}
		println ""
		println "stacks: $stacks"
	}
	
	def new_hand(id) {
		Log.info("*** new Hand ($id) ***")
		pot = 0
		board = []
		raises = 0
		inc_button()
		deck.reset().shuffle()
		players.each { p-> 
			actions[p.name] = Action.waiting()
			bets[p.name] = 0
		}
		players*.new_hand()
		Log.info("dealer is ${players[button].name}")
	}
	
	def is_noshowdown() {
		actions.findAll { k,v -> v.type == Action.FOLD }.size() == players.size() - 1 ? true : false
	}
	
	def set_waiting() {
		actions.each {k,v ->if (v.type != Action.FOLD) actions[k] = Action.waiting()}
	}
	
	def get_bb_seat() {
		(button + 2) % players.size()
	}
	
	def get_sb_seat() {
		(button + 1) % players.size()
	}
	
	def get_bb_size() {
		conf.bigblind
	}
	
	def get_sb_size() {
		conf.smallblind
	}
	
	def get_num_active() {
		(actions.findAll {k,v-> v.type != Action.FOLD }).size()
	}
	
	def preflop() {
		Log.debug("* preflop")
		game_stage = PREFLOP
		def sbname = players[get_sb_seat()].name
		def bbname = players[get_bb_seat()].name
		actions[sbname] = Action.smallblind(conf.smallblind)
		actions[bbname] = Action.bigblind(conf.bigblind)
		
		stacks[sbname] -= conf.smallblind
		stacks[bbname] -= conf.bigblind
		
		bets[sbname] += conf.smallblind
		bets[bbname] += conf.bigblind
		
		pot = conf.bigblind + conf.smallblind
		
		players.each { p->
			def c1 = deck.deal()
			def c2 = deck.deal()
			Log.debug ("${p.name} hole cards: ${c1} ${c2}")
			p.hole_cards(c1,c2)
		}
		
		playout_hand()
		is_noshowdown()
	}
	
	def flop() {
		Log.debug("* Flop")
		game_stage = FLOP
		def c1=deck.deal(),c2=deck.deal(),c3=deck.deal()
		board << c1 << c2 << c3
		Log.info ("board flop: ${board}")
		playout_hand()
		is_noshowdown()
	}
	
	def river() {
		Log.debug("* River")
		game_stage = RIVER
		board << deck.deal()
		Log.info ("board river: ${board}")
		playout_hand()
		is_noshowdown()
	}
	def turn() {
		Log.debug("* Turn")
		game_stage = TURN
		board << deck.deal()
		Log.info ("board turn: ${board}")
		playout_hand()
		is_noshowdown()
	}
	def showdown() {
		Log.debug("* Showdown")
		game_stage = SHOWDOWN
		def pockets = players.collect { it.get_hand() }
		def winners = SpadesEval.winners(pockets, board)
		winners.each { w->
			def name = players[w].name
			stacks[name] += (pot / winners.size())
			Log.info "$name won ${pot/winners.size()}"
		}
		Log.info "stacks: $stacks"
		return is_noshowdown()
	}
	def playout_hand() {
		current_player = get_first_player()
		if (game_stage != PREFLOP) players.each { bets[it.name] = 0 }
		set_waiting()
		while(!is_betting_over()) {
			def curname = players[current_player].name
			if (actions[curname].type == Action.FOLD) {
				inc_player()
				Log.info("${curname} had folded skipping.")
				continue
			}
			def ac = players[current_player].get_action()
			interprete_action(ac)
			Log.debug ("player ${curname} -> ${Action.get_string(ac.type)} Bets: ${bets} Stacks: ${stacks}")
			actions[curname] = ac
			players*.player_action(ac)
			inc_player()
		}
				
	}
	def is_betting_over() {
		//all but 1 folded
		if (is_noshowdown()) return true
		//everybody checked?
		def checks = actions.findAll {k,v-> v.type == Action.CHECK || v.type == Action.FOLD }
		//def folds = actions.values().findAll { it.type == Action.FOLD }
		if (checks.size() == players.size()/* - folds.size()*/) return true
		def waiting = actions.findAll {k,v-> v.type == Action.WAITING || 
			v.type == Action.BIGBLIND || v.type == Action.SMALLBLIND }
		if (waiting.size() > 0) return false
		//find the players not folded to compare bets
		def notfolded = actions.findAll { k,v-> v.type != Action.FOLD }
		//get the not folded players bets
		def fbets = notfolded.collect {k,v -> bets[k]}
		def over = fbets.findAll { it != fbets.max() }
		if (over.size() != 0) return false
		return true
	}
	
	def get_to_call(seat) {
		def b = bets.values().max()
		def p = players[seat].name
		return b - bets[p]
	}
	
	def get_min_raise(seat) {
		raises < conf.maxraises ? conf.bigblind : 0
	}
	
	def interprete_action(ac) {
		def curname = players[current_player].name
		if (ac.type == Action.RAISE) {
			raises++
			pot += ac.tocall + ac.amount
			bets[curname] += ac.tocall + ac.amount
			stacks[curname] -= ac.tocall + ac.amount
		}
		if (ac.type == Action.BET) {
			pot += ac.amount
			bets[curname] += ac.amount
			stacks[curname] -= ac.amount
		}
		if (ac.type == Action.CALL) {
			pot += ac.tocall
			bets[curname] += ac.tocall
			stacks[curname] -= ac.tocall
		}
	}
	
	def load_players() {
		players << new Callbot(this, 0, "callbot1")
		players << new Callbot(this, 1, "callbot2")
		assert players.size() == conf.numplayers
		players*.game_start()
		players.eachWithIndex {v,i -> stacks[v.name] = conf.stack }
	}
	
	def steal_pot() {
		actions.eachWithIndex {k,v,i ->
			if (v.type != Action.FOLD) {
				stacks[k] += pot
				Log.info("$k stole $pot")
				return
			}
		}
	}
	
	def inc_button() {
		button = (button+1) % players.size()
		return button
	}
	
	def inc_player() {
		current_player = (current_player + 1) % players.size()
		return current_player
	}
	
	def get_first_player() {
		def delta = game_stage == PREFLOP ? 3 : 1
		return (button + delta) % players.size()
	}
	
	public static void main(args) {
		println "Botpit v2"
		def t1 = System.currentTimeMillis()
		new Botpit().run()
		print "duration (ms): ${System.currentTimeMillis() - t1}" 
	}
}