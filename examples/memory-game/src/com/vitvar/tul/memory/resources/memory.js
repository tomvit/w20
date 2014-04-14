
var card1 = null;
var card2 = null;
var gameBaseUri = null;
var input;
var board;
var allowClick = true;

// init the game when the content is loaded
window.onload = function() {
	// main board and input element
	board = document.getElementById("board");
	
	// players information
	players = document.getElementById("players");
	
	// input field - number of cards
	input = document.getElementById("input");	

	// when the URL contains a hash, assume user wants to 
	// refresh or load existing game instance
	if (window.location.hash != "") {
		gameBaseUri = "/api/games/" + window.location.hash.substring(1);
		input.style.display = "none";
		initSSE();
	}
}

// utility function to invoke a game API using XHR
function invokeAPI(method, uri, data, callback) {
	var x = new XMLHttpRequest();
	
	// callback to read the response
	x.onreadystatechange = function() {
		if (x.readyState == 4) {
			data = null;
			if (x.responseText != "")
				data = JSON.parse(x.responseText);			
			if (callback)
				callback(x, this.status, data);
		} 		 	
	}
	
	x.open(method, uri, true);
	x.send(data);
}

// redraw the information about players using the status data
function drawPlayers(data) {
	// draw players according to the status
	ps = "";
	for (i = 0; i < data.players.length; i++) {
		var clazz = "";
		if (data.players[i].active)
			clazz = "active";
		ps += "<span id=\"player\" class=\"" + clazz + "\">Player " + (i + 1) + ": " + 
			data.players[i].name +", cards=" + data.players[i].numCards + "&nbsp;&nbsp;</span>"
	}
	players.innerHTML = ps;	
}

// redraw the board using the status data
function drawBoard(data) {
	if (data.game_over) {
		board.innerHTML = "GAME OVER, the winner is Player " + (data.winner + 1); 
	} else {
		c1 = null; c2 = null;
		// draw the board according to the status
		bs = "";
		for (i = 0; i < data.cards.length; i++) {
			var clazz = "back";
			if (data.cards[i].value == 0)
				clazz = "away";
			if (data.cards[i].turned)
				clazz = "front";
			bs += "<div id=\"card" + i + "\" class=\"" + clazz + 
				"\" onclick=\"javascript:clickCard(this)\">" + (data.cards[i].turned ? data.cards[i].value : "?") + "</div>";
			if (data.cards[i].turned) {
				if (c1 == null)
					c1 = i;
				else
					if (c2 == null)
						c2 = i;
			}
		}
		board.innerHTML = bs;
		
		// update information about turned cards
		if (c1 != null) card1 = "card"+c1;
		if (c2 != null) card2 = "card"+c2;
		
		// if both cards are turned than make a move after 500 ms
		// 500 ms is a minimal delay allowing a user to read the value of the card
		if (c1 != null && c2 != null) {
			setTimeout(function() {
				card1 = null;
				card2 = null;
				invokeAPI("POST", gameBaseUri + "/moves");					
			}, 500);
		}
	}
}

// init server-side includes; there is no fallback to polling when SSE is not supported by the browser!
function initSSE() {
	if (window.location.hash != "") {
		
		var source = new EventSource("/api/games/" + window.location.hash.substring(1) + "/realtime/status/");
		
		// whenever new status data is available, refresh players and board
		source.addEventListener('message', function(e) {
			//alert(e.data);
			data = JSON.parse(e.data);
			drawPlayers(data);
			drawBoard(data);
			allowClick = true;
		}, false);					
	}
}

// this will create a new game instance when user enters number of cards
// this is not called when user only refreshes existing game instance
function initGame() {	
	// input resource for the game instance to be created
	// number of players is not currently used
	inputdata = "{ \"numCards\" : " + document.getElementById("numcards").value + ", " + 
		"\"numPlayers\" : 0 }";
	
	invokeAPI("POST", "/api/games", inputdata,
		function(xhr, status, data) {
			if (status == 201) { // created
				gameBaseUri = xhr.getResponseHeader("Location");
				
				var p= new RegExp("^/api/games/([0-9a-fA-F]+)$");
				m = gameBaseUri.match(/^\/api\/games\/([0-9a-fA-F]+)$/);
				if (m != null) {
					window.location.hash = m[1];
					input.style.display = "none";
					initSSE();
				}
			} else
				; // display error message			
		}
	);
	
	return false;
}

// this function is called when a card is called
// this will call "turn" operation and consequently change game status
// that will be refreshed by SSE callback
function clickCard(card) {
	if (!allowClick || card.className == "away" || card.id == card1)
		return;	
	
	allowClick = false;	
	invokeAPI("POST", gameBaseUri + "/turns", 
			"{ \"CardInx\" : " + card.id.substring(4) + " }", 			
			function(xhr, status, data) {
				if (status == 200) {
					if (card1 == null) {
						card1 = card.id;
					} else
						if (card2 == null) {
							card2 = card.id;
						}
				}
			});
}
