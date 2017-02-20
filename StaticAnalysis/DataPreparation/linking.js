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

			result.linked[classToLink] = linkModule.moduleName;
		}
		else {
			result.unlinked.push(classToLink);
		}
	}

	return result;
}

if (process.argv.length != 4)
	console.log("Please specify folder with data for linkage and resulting folder!");
else {
	var sourceFolder = process.argv[2];
	var resultFolder = process.argv[3];

	var files = fs.readdirSync(sourceFolder);

	var dataFiles = [];
	files.forEach(
		function (file) {
			dataFiles.push(JSON.parse(fs.readFileSync(sourceFolder+"/"+file)));
		}
	);

	var dataArray = [];

	for (var i = 0; i < dataFiles.length; i++) {
		var moduleName = dataFiles[i].sourceDir.split("/").pop();
		var data = {
			"moduleName": moduleName,
			"linked": {},
			"required": [],
			"unlinked": dataFiles[i].dependencies,
			"provided": dataFiles[i].provided
		};

		dataArray.push(link(data,data));
	}

	var result = [];
	var miniResult = [];

	for (var i = 0; i < dataArray.length; i++) {
		var temp = dataArray[i];
		console.log((i+1) + " from " + dataArray.length + " = " + temp.moduleName);

		for (var j = 0; j < dataArray.length; j++) {
			temp = link(temp, dataArray[j]);
		}

		result.push(temp);

		var specialMap = {};
		for (var key in temp.linked) {
			if (temp.linked.hasOwnProperty(key)) {
				var requiredModuleName = temp.linked[key];

				if (specialMap[requiredModuleName] === undefined) specialMap[requiredModuleName] = 0;

				specialMap[requiredModuleName] += 1;
			}
		}

		miniResult.push({"moduleName": temp.moduleName, "unlinked": temp.unlinked, "required": temp.required, "moduleUsage": specialMap});
	}

	fs.writeFileSync(resultFolder+"/"+"fullData.json", JSON.stringify(result, null, 4));
	fs.writeFileSync(resultFolder+"/"+"miniData.json", JSON.stringify(miniResult, null, 4));
	console.log("Result files saved!")
}