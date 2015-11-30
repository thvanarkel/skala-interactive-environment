var osc = require('node-osc');
var _ = require('lodash');
var oscServer = new osc.Server(12000, '127.0.0.1');
var SerialPort = require("serialport").SerialPort
var serialPort = new SerialPort("COM7", {
	baudrate: 9600
});

var MAX_DIST = 0.3,
	MOVEMENT_TIME = 30;

var S_IDLE = 0,
	S_RUSTLE = 4,
	S_SWING = 1;
	S_FLAP = 2;

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

var users = {};

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
	return minDistance(user, servo);
}

function parseMsg(msg) {
	return {
		type: msg[2][0],
		pid: msg[2][1],
		oid: msg[2][2],
		age: msg[2][3],
		centroidX: msg[2][4],
		centroidY: msg[2][5],
		velocityX: msg[2][6],
		velocityY: msg[2][7],
		depth: msg[2][8],
		boundingRectX: msg[2][9],
		boundingRectY: msg[2][10],
		boundingRectWidth: msg[2][11],
		boundingRectHeight: msg[2][12],
		highestX: msg[2][13],
		highestY: msg[2][14],
		haarRectX: msg[2][15],
		haarRectY: msg[2][16],
		haarRectWidth: msg[2][17],
		haarRectHeight: msg[2][18],
		opticalFlowVectorAccumulationX: msg[2][19],
		opticalFlowVectorAccumulationY: msg[2][20]
	};
}

function loop() {

	_.each(servos, function(servo, id) {
		servo.state = S_IDLE;
	});

	_.each(users, function(user, pid) {
		var x = Math.abs(user.velocityX);
		var y = Math.abs(user.velocityY);
		console.log(x, y);
		if (x < 3 && y < 3) {
			user.timer += (10 - (x + y));
		} else /*if(x > 6 || y > 6)*/ {
			user.timer -= ((x + y) / 2);
		}


		// var x = user.velocityX;
		// var y = user.velocityY;

		// if(x < 0.1 && y < 0.1){
		// 	user.timer += 0.2 - (x + y);
		// } else if(x > 0.5 || y > 0.5) {
		// 	user.timer -= (x + y) / 2;
		// }

		user.timer = Math.min(user.timer, MOVEMENT_TIME);
		user.timer = Math.max(user.timer, 0);

		console.log(user.pid + ": " + (user.boundingRectX + user.boundingRectWidth));

		_.each(servos, function(servo, id) {
			var dist = distance(user, servo);
			if (dist < MAX_DIST) {
				var d = 1 - (MAX_DIST - dist) / MAX_DIST;
				if (user.timer < MOVEMENT_TIME) {
					var t = (MOVEMENT_TIME - user.timer) / MOVEMENT_TIME,
						i = 1 - Math.abs(d - t);
					rustle(servo, i);
				} else {
					var closest = _.first(_.sortBy(servos, function(s,i){
						return distance(user, s)
					}));
					if (servo.id == closest.id) {
						flap(servo);
					} else {
						idle(servo);
					}
				}
			} else {
				idle(servo);
			}
		})
	});

	_.each(servos, function(servo, id) {
		// if(servo.state !== S_IDLE)
		// console.log(message);
		var message = new Buffer([servo.id, servo.state]);
		serialPort.write(message);
	});

}

function idle(servo) {
	if (servo.state > S_IDLE) return;

	// console.log('idling ', servo.id);
	servo.state = S_IDLE;
}

function rustle(servo, i) {
	if (servo.state > S_RUSTLE) return;
	// console.log('rustling ', servo.id, i);
	servo.state = S_RUSTLE;

}

function flap(servo) {
	if (servo.state > S_FLAP) return;
	console.log('flapping ', servo.id);
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
	console.log('user ' + u.pid + ' left after ' + u.age);
	delete users[msg.pid];
}

oscServer.on("message", function(msg, rinfo) {

	msg = parseMsg(msg);
	// console.log(msg);

	switch (msg.type) {
		case '/TSPS/personEntered/':
			newUser(msg);
			break;
		case '/TSPS/personUpdated/':
			updateUser(msg);
			break;
		case '/TSPS/personWillLeave/':
			destroyUser(msg);
			break;
		default:
	}

});

serialPort.on("open", function() {
	console.log('open');
	setInterval(loop, 1000);
})