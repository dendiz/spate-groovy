package core;

class Action {
	static BIGBLIND = 0
	static SMALLBLIND = 1
	static FOLD = 2
	static CHECK = 3
	static CALL = 4
	static BET = 5
	static RAISE = 6
	static WAITING = 7
	def type, tocall, amount
	public Action(type, tocall, amount) {
		this.type = type
		this.tocall = tocall
		this.amount = amount
	}
	
	String toString(action) {
		def str = ["bigblind","smallblind","fold","check","call","bet","raise","waiting"]
		return str[action]
	}
	
	static String get_string(action) {
		def str = ["bigblind","smallblind","fold","check","call","bet","raise","waiting"]
		return str[action]		
	}
	
	static bigblind(topost) {
		return new Action(BIGBLIND, 0, topost)
	}
	
	static smallblind(topost) {
		return new Action(SMALLBLIND, 0, topost);
	}
	
	static fold(gi) {
		return new Action(FOLD, gi.get_to_call(gi.current_player), 0)
	}
	
	static check(gi) {
		return new Action(CHECK, 0, 0)
	}
	
	static check_or_fold(gi) {
		if (gi.get_to_call(gi.current_player) == 0) return Action.check(gi)
		return new Action(FOLD, gi.get_to_call(gi.current_player), 0)		
	}
	
	static call(gi) {
		if (gi.get_to_call(gi.current_player) == 0) return Action.check(gi)
		return new Action(CALL, gi.get_to_call(gi.current_player), 0)
	}
	
	static waiting() {
		return new Action(WAITING, 0, 0)
	}
	
	static bet(gi) {
		return new Action(BET, 0, gi.get_bb_size())
	}
	
	static raise(gi) {
		def cp = gi.current_player
		if (gi.get_min_raise(cp) == 0) return Action.call(gi)
		if (gi.get_to_call(cp) == 0) return Action.bet(gi)
		return new Action(RAISE, gi.get_to_call(cp), gi.get_min_raise(cp))
	}
}