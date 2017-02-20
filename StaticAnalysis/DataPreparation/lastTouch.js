"use strict";
var fs = require('fs');

if (process.argv.length != 4)
	console.log("Please specify input file and result file!");
else {
	var inputFile = process.argv[2];
	var resultFile = process.argv[3];

	var data = fs.readFileSync(inputFile, "utf8");

	fs.writeFileSync(resultFile, "var inputData = " + data +";");
	console.log("Result file saved!")
}