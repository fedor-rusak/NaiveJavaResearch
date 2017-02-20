"use strict";
var fs = require('fs');
var execSync = require('child_process').execSync;

if (process.argv.length != 5)
	console.log("Please specify script, folder with data and resulting folder!");
else {
	var scriptName =  process.argv[2];
	var sourceFolder = process.argv[3];
	var resultFolder = process.argv[4];

	var files = fs.readdirSync(sourceFolder);

	var prefix = "prepare" === scriptName ? ".json" : "";

	files.forEach(
		function (file) {
			var resultPath = resultFolder+"/"+file+prefix;
			console.log(file);
			execSync("node " + scriptName + " "+sourceFolder+"/"+file+ " " + resultPath, {"stdio":[0,1,2]});
		}
	);

	console.log("Performed "+scriptName+" on all data! " + files.length);
}