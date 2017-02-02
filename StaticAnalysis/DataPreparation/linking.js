"use strict";
var fs = require('fs');

function copy(dataObject) {
	return JSON.parse(JSON.stringify(dataObject))
}

function hasValue(array, value) {
	var result = false;

	for (var i = 0; i < array.length; i++) {
		if (array[i] === value) {
			result = true;
			break;
		}
	}

	return result;
}

function link(targetModule, linkModule) {
	var result = {
		"moduleName": targetModule.moduleName,
		"linked": copy(targetModule.linked),
		"required": copy(targetModule.required),
		"unlinked": [],
		"provided": copy(targetModule.provided)
	};

	for (var i = 0; i < targetModule.unlinked.length; i++) {
		var classToLink = targetModule.unlinked[i];

		if (hasValue(linkModule.provided, classToLink)) {
			if (hasValue(result.required, linkModule.moduleName) === false) {
				result.required.push(linkModule.moduleName);
			}

			result.linked[classToLink] = result.required.length-1;
		}
		else {
			result.unlinked.push(classToLink);
		}
	}

	return result;
}

if (process.argv.length == 2)
	console.log("Please specify folder with data for preparation!");
else {
	var dataFiles = [];
	for (var i = 2; i < process.argv.length; i++) {
		dataFiles.push(JSON.parse(fs.readFileSync(process.argv[i])));
	}

	var dataArray = [];

	for (var i = 0; i < dataFiles.length; i++) {
		var data = {
			"moduleName": dataFiles[i].sourceDir,
			"linked": {},
			"required": [],
			"unlinked": dataFiles[i].dependencies,
			"provided": dataFiles[i].provided
		};

		dataArray.push(data);
	}

	var result = [];
	var miniResult = [];

	for (var i = 0; i < dataArray.length; i++) {
		var temp = dataArray[i];

		for (var j = 0; j < dataArray.length; j++) {
			temp = link(temp, dataArray[j]);
		}
		// temp.linked = undefined;
		// temp.unlinked = undefined;
		// temp.provided = undefined;
		result.push(temp);

		miniResult.push({"moduleName": temp.moduleName, "unlinked": temp.unlinked, "required": temp.required});
	}

	fs.writeFileSync("finalResult.json", JSON.stringify(result, null, 4));
	fs.writeFileSync("finalMiniResult.json", JSON.stringify(miniResult, null, 4));
	console.log("Result file saved!")
}