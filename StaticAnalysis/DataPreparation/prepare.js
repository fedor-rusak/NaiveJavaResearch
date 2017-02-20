"use strict";
var fs = require('fs');

var listFiles = function(dir) {
	var folderStack = [dir];
	var resultFilePaths = [];

	while (folderStack.length > 0) {
		var currentFolder = folderStack.pop();

		var paths = fs.readdirSync(currentFolder);

		for (var i = 0; i < paths.length; i++) {
			var currentPath = currentFolder + "/" + paths[i];

			if (fs.statSync(currentPath).isDirectory()) {
				folderStack.push(currentPath);
			}
			else
				resultFilePaths.push(currentPath);
		}
	}

	return resultFilePaths;
};

function searchInPool(pool, index) {
	var result;

	for (var i = 0; i < pool.length; i++) {
		if (index === pool[i].index) {
			result = pool[i];
			break;
		}
	}

	return result;
}

function prepareDataObject(data) {
	var indent = "   ";
	//console.log(indent + "thisClass: " + data.thisClassIndex);

	var thisClassConstantIndex = searchInPool(data.constantPool, data.thisClassIndex).classIndex;
	//console.log(indent + "thisClassConstantIndex: " + thisClassConstantIndex);

	var thisClassName = searchInPool(data.constantPool, thisClassConstantIndex).value;
	//console.log(indent + "thisClass: " + thisClassName);

	//console.log(indent + "superClass: " + data.superClassIndex);

	var superClassName;
	if (thisClassName !== "java/lang/Object") {
		var superClassConstantIndex = searchInPool(data.constantPool, data.superClassIndex).classIndex;
		//console.log(indent + "superClassConstantIndex: " + superClassConstantIndex);

		superClassName = searchInPool(data.constantPool, superClassConstantIndex).value;
		//console.log(indent + "superClass: " + superClassName);
	}
	else {
		superClassName ="DOES_NOT_EXIST!!!";
	}

	var dependencies = [];

	for (var i = 0; i < data.constantPool.length; i++) {
		var classConstantIndex = data.constantPool[i].classIndex;

		if (classConstantIndex && classConstantIndex !== thisClassConstantIndex) {
			dependencies.push(searchInPool(data.constantPool, classConstantIndex).value);
		}
	}

	return {
		"thisClassName": thisClassName,
		"superClassName": superClassName,
		"dependencies": dependencies
	}
}

if (process.argv.length != 4)
	console.log("Please specify folder with data for preparation and result file name!");
else {
	var sourceDir = process.argv[2];
	var resultFileName = process.argv[3];
	var filesToPrepare = listFiles(sourceDir);

	var jsonToPrepare = [];

	for (var i = 0; i < filesToPrepare.length; i++) {
		jsonToPrepare.push(JSON.parse(fs.readFileSync(filesToPrepare[i])));
	}

	var result = [];

	for (var i = 0; i < jsonToPrepare.length; i++) {
		result.push(prepareDataObject(jsonToPrepare[i]));
	}

	fs.writeFileSync(resultFileName, JSON.stringify({"sourceDir": sourceDir, "data": result}, null, 4));
	console.log("Result file saved!")
}