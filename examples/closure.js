
function adder() {
	sum = 0;
	return function(x) {
		sum += x;
		return sum;
	}
}

var pos = adder();
console.log(pos(3)); // returns 3 
console.log(pos(4)); // returns 7
console.log(pos(5)); // returns 12

