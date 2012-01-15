package bot;

import core.Action;

class Callbot {
	def gi,seat,name,c1,c2
	public Callbot(gi, seat, name) {
		this.gi = gi
		this.seat = seat
		this.name = name
	}
	
	public get_action() {
		return Action.call(gi);
	}
	public game_start() {
		
	}
	
	public player_action(ac) {
		
	}
	public hole_cards(c1, c2) {
		this.c1 = c1
		this.c2 = c2
	}
	public new_hand() {
		
	}
	
	def get_hand() {
		return [c1,c2]
	}
}