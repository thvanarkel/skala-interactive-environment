var MAX_DIST = 0.3,
	MOVEMENT_TIME = 30;

var S_IDLE = 0,
	S_RUSTLE = 1,
	S_SWING = 2;
	S_FLAP = 4;

var servos = [{
	id: 0,
	x: 0.87,
	y: 0.9,
	state: S_IDLE
}, {
	id: 1,
	x: 0.775,
	y: 0.9,
	state: S_IDLE
}, {
	id: 2,
	x: 0.47,
	y: 0.83,
	state: S_IDLE
}, {
	id: 3,
	x: 0.35,
	y: 0.8,
	state: S_IDLE
}, {
	id: 5,
	x: 0.80,
	y: 0.54,
	state: S_IDLE
}, {
	id: 4,
	x: 0.59,
	y: 0.49,
	state: S_IDLE
}, {
	id: 10,
	x: 0.86,
	y: 0.06,
	state: S_IDLE
}, {
	id: 7,
	x: 0.59,
	y: 0.15,
	state: S_IDLE
}, {
	id: 6,
	x: 0.5,
	y: 0.16,
	state: S_IDLE
}, {
	id: 8,
	x: 0.35,
	y: 0.3,
	state: S_IDLE
}, {
	id: 9,
	x: 0.29,
	y: 0.305,
	state: S_IDLE
}];

var container = $('#container');
var timer = 0;

var users = {};

for (var i = 0; i < 20; i++) {
	for (var j = 0; j < 20; j++) {
		var child = $('<div>')
			.attr("id", 'cell-' + i + '-' + j)
			.data("pos", JSON.stringify({
				x: 50 * j + 25,
				y: 50 * i + 25
			}))
			// .toggleClass('hasLadder', Math.random()>0.9)
		container.append(child);
	}
}

for(var idx in servos){
	var ladder = servos[idx];
	var x = Math.round(ladder.x * 20);
	var y = Math.round(ladder.y * 20);
	$('#cell-' + x + '-' + y).toggleClass('hasLadder', true);
}

function minDistance(user, servo) {
	return _.min([
		// dist(user.boundingRectX, user.boundingRectY, servo.x, servo.y),
		// dist(user.boundingRectX, user.boundingRectY + user.boundingRectHeight, servo.x, servo.y),
		// dist((user.centroidX + user.highestX + user.highestX + user.highestX)/4, user.centroidY, servo.x, servo.y),
		// dist(user.highestX, user.centroidY, servo.x, servo.y),
		dist(user.boundingRectX + user.boundingRectWidth, user.centroidY, servo.x, servo.y)
	]);
}

function dist(x1, y1, x2, y2) {
	return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
}

function distance(user, servo) {
	return dist(user.x, user.y, servo.x, servo.y);
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

	var ladders = container.children('.hasLadder').toArray();

	_.each(ladders, function(l){
		l.state = S_IDLE;
		idle(l);
	})

	_.each(users, function(user, idx){
		if(user.dx + user.dy > 4){
			user.timer -= (user.dx + user.dy)/2;
		} else {
			user.timer += 5;
		}

		user.timer = Math.max(Math.min(user.timer, 100), 0);

		var sorted = _.slice(_.sortBy(servos, function(cell) {
			dist = distance(user, pos);
		}), 0, 11);
		
		var ladderIdx = NUM_LADDERS - Math.floor(user.timer/(NUM_LADDERS - 1)) - 1;
		var flapper = sorted[ladderIdx];

		console.log(ladderIdx);

		if(ladderIdx < 1){
			flap(flapper);
		} else if(ladderIdx < 4){
			swing(flapper);
		} else {
			rustle(flapper);
		} 

		user.dx = user.dy = 0;
	})
}

function idle(servo) {
	if (servo.state > S_IDLE) return;

	$(servo).toggleClass('rustling', false);
	$(servo).toggleClass('swinging', false);
	$(servo).toggleClass('flapping', false);

	console.log('idling ', servo.id);
	servo.state = S_IDLE;
}

function rustle(servo, i) {
	if (servo.state > S_RUSTLE) return;

	$(servo).toggleClass('rustling', true);
	$(servo).toggleClass('swinging', false);
	$(servo).toggleClass('flapping', false);

	console.log('rustling ', servo.id, i);
	servo.state = S_RUSTLE;
}

function swing(servo) {
	if (servo.state > S_SWING) return;
	$(servo).toggleClass('rustling', false);
	$(servo).toggleClass('swinging', true);
	$(servo).toggleClass('flapping', false);
	console.log('swinging ', servo.id, i);
	servo.state = S_SWING;
}


function flap(servo) {
	if (servo.state > S_FLAP) return;
	$(servo).toggleClass('rustling', false);
	$(servo).toggleClass('swinging', false);
	$(servo).toggleClass('flapping', true);
	console.log('flapping ', servo.id, i);
	servo.state = S_FLAP;
}

function newUser(msg) {
	users[msg.pid] = msg;
	users[msg.pid].timer = 0;
}

function updateUser(msg) {
	if (!users[msg.pid]) {
		newUser(msg);
	}
	var t = users[msg.pid].timer;
	var u = users[msg.pid] = msg;
	u.timer = t;
}

function destroyUser(msg) {
	if (!users[msg.pid]) return;
	var u = users[msg.pid];
	// console.log('user ' + u.pid + ' left after ' + u.age);
	delete users[msg.pid];
}

container.on('mouseenter', function(e){
	// console.log("ENTER")
	users[0] = {
		x: 0,
		y: 0,
		dx: 0,
		dy: 0,
		timer: 0
	}
});

container.on('mousemove', function(e) {
	user = users[0];

	user.x = e.layerX;
	user.y = e.layerY;
	user.dx += e.movementX;
	user.dy += e.movementY;
})

window.setInterval(loop, 200);