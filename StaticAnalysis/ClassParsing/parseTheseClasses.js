"use strict";
var fs = require('fs');
var execSync = require('child_process').execSync;

if (process.argv.length != 4)
	console.log("Please specify folder with data and resulting folder!");
else {
	var libFolder = process.argv[2];
	var resultFolder = process.argv[3];

	var files = fs.readdirSync(libFolder);

	files.forEach(
    	function (file) {
    		var resultPath = resultFolder+"/"+file.replace(".jar", "");
			console.log(file);
    		execSync("call_parsing "+libFolder+"/"+file+ " " + resultPath, {"stdio":[0,1,2]});
		}
	);

	console.log("Parsed some data! Number of components: " + files.length);
}