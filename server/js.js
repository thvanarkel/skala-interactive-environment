
var servos = [{
	id: 0,
	x: 0.8,
	y: 1 - 0.1
}, {
	id: 1,
	x: 0.75,
	y: 1 - 0.1
}, {
	id: 2,
	x: 0.2,
	y: 1 - 0.15
}, {
	id: 3,
	x: 0.1,
	y: 1 - 0.15
}, {
	id: 4,
	x: 0.75,
	y: 1 - 0.5
}, {
	id: 5,
	x: 0.5,
	y: 1 - 0.5
}, {
	id: 6,
	x: 0.25,
	y: 1 - 1
}, {
	id: 7,
	x: 0.45,
	y: 1 - 1
}, {
	id: 8,
	x: 0.1,
	y: 1 - 1
}, {
	id: 9,
	x: 0,
	y: 1 - 1
}, {
	id: 10,
	x: 0.8,
	y: 1 - 1
}, ];

var MAX_DIST = 400,
	MOVEMENT_TIME = 400;

var container = $('#container');

var upos = {
		x: 0,
		y: 0
	},
	timer = 0;


for (var i = 0; i < 20; i++) {
	for (var j = 0; j < 20; j++) {
		var child = $('<div>')
			.attr("id", 'cell-' + i + '-' + j)
			.data("pos", JSON.stringify({
				x: 50 * i + 25,
				y: 50 * j + 25
			}))
			.toggleClass('hasLadder', Math.random()>0.9)
		container.append(child);
	}
}

// for(var idx in servos){
// 	var ladder = servos[idx];
// 	var x = Math.round(ladder.x * 20);
// 	var y = Math.round(ladder.y * 20);
// 	$('#cell-' + x + '-' + y).toggleClass('hasLadder', true);
// }

function getDist(pos) {
	return Math.sqrt(Math.pow(upos.x - pos.x, 2) + Math.pow(upos.y - pos.y, 2));
}

function reset() {
	timer = 0;
}

function rustle(cell, intensity) {
	cell.toggleClass('rustling', intensity < 0.1);
	cell.css("opacity", Math.max(0, intensity-0.8)*2);
	cell.removeClass('flapping');
}

function flap(cell) {
	cell.css("opacity", 1);
	cell.removeClass('rustling');
	cell.addClass('flapping');
}

function idle(cell) {
	cell.css("opacity", 0);
	cell.removeClass('rustling');
	cell.removeClass('flapping');
}

function loop() {
	timer++;


	// for(var i = 0; i < 20; i++) {
	// 	for(var j = 0; j < 20; j++) {
	// 		var cell = $('#cell-'+i+'-'+j).eq(0),
	container.children('.hasLadder').forEach(function(cell, i){
		if(!cell)return;
		cell = $(cell);
		pos = cell.data('pos');

		var dist = getDist(pos);
		if(dist < MAX_DIST) {
			var d = 1 - (MAX_DIST - dist)/MAX_DIST;
			if(timer < MOVEMENT_TIME){
					var t = (MOVEMENT_TIME - timer)/MOVEMENT_TIME,
						i = 1 - Math.abs(d - t)/d;
				rustle(cell, i);
			} else {
				if(d < 0.2){
				flap(cell);
			} else {
				idle(cell);
			}
			}
		} else {
			idle(cell);
		}
	})

	// 	}
	// }
}

container.on('mousemove', function(e, f, g) {
	var cell = $(e.target),
		pos = cell.data('pos');

	if(!pos)return;

	if (upos.x !== pos.x || upos.y !== pos.y) {
		reset();
	}

	upos = pos;
})

window.setInterval(loop, 20);