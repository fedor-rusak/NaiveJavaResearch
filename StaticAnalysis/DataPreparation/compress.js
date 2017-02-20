"use strict";
var fs = require('fs');

if (process.argv.length != 4)
	console.log("Please specify folder with data for preparation and resulting file name!");
else {
	var moduleFileData = JSON.parse(fs.readFileSync(process.argv[2]));
	var resultFileName = process.argv[3];

	console.log(moduleFileData.data.length);

	var result = {
		"provided" : {},
		"providedClasses" : [],
		"dependencies": {},
		"dependencyClasses": []
	}

	for (var i = 0; i < moduleFileData.data.length; i++) {
		var classFile = moduleFileData.data[i];

		result.provided[classFile.thisClassName] = i;
		result.providedClasses.push(classFile.thisClassName);

		for (var j = 0; j < classFile.dependencies.length; j++) {
			var dependencyClass = classFile.dependencies[j].replace(new RegExp("\\[+L"), "").replace(";", "");

			if (dependencyClass[0] !== "[" 
				&& result.dependencies[dependencyClass] === undefined 
				&& classFile.thisClassName !== dependencyClass) {

				result.dependencyClasses.push(dependencyClass);

				result.dependencies[dependencyClass] = result.dependencyClasses.length-1;
			}
		}
	}

	var resultString = JSON.stringify(
		{
			"sourceDir": moduleFileData.sourceDir,
			"provided" : result.providedClasses,
			"dependencies": result.dependencyClasses
		},
		null, 4);

	fs.writeFileSync(resultFileName, resultString);
	console.log("Result file saved!")
}