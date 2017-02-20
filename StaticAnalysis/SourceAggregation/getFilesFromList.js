"use strict";
var fs = require('fs');
var path = require('path');

function copyFileSync(source, target) {
    var targetFile = target;

    //if target is a directory a new file with the same name will be created
    if (fs.existsSync(target)) {
        if (fs.lstatSync(target).isDirectory()) {
            targetFile = path.join(target, path.basename(source));
        }
    }

    fs.writeFileSync(targetFile, fs.readFileSync(source));
}

function copyFolderRecursiveSync(source, target) {
    var files = [];

    //check if folder needs to be created or integrated
    var targetFolder = path.join(target, path.basename(source));
    if (!fs.existsSync(targetFolder)) {
        fs.mkdirSync(targetFolder);
    }

    //copy
    if (fs.lstatSync(source).isDirectory()) {
        files = fs.readdirSync(source);
        files.forEach(
        	function (file) {
				var curSource = path.join(source, file);
				if ( fs.lstatSync(curSource ).isDirectory()) {
					copyFolderRecursiveSync(curSource, targetFolder);
				}
				else {
					copyFileSync(curSource, targetFolder);
				}
        	}
        );
    }
}


if (process.argv.length == 2)
	console.log("Please specify file with data!");
else {
	var buildFile = fs.readFileSync(process.argv[2], "utf8");

	var buildFile = buildFile.replace(new RegExp("\r", "g"), "");
	var lines = buildFile.split("\n");

	for(var i = 0; i < lines.length; i++) {
		var line = lines[i];

		if (line.endsWith(".jar")) {
			var jarName = line.split("/")[line.split("/").length-1];

			var jarFilePath = line.replace("\\","").replace(new RegExp("/", "g"), "\\\\");

			copyFileSync(jarFilePath, 'lib/'+jarName);
		}
		else {
			var classFolderName = line.split("/")[line.split("/").length-1];

			var classFolderNamePath = line.replace("\\","").replace(new RegExp("/", "g"), "\\\\");

			copyFolderRecursiveSync(classFolderNamePath, "lib/");
		}
	}

	console.log("Downloaded all " + lines.length + " components!");
}