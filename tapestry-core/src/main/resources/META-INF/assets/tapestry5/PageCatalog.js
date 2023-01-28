console.log("PageCatalog.js");
function showGraphviz(pageName, dot) {
	var hpccWasm = require("https://cdn.jsdelivr.net/npm/@hpcc-js/wasm/dist/graphviz.umd.js");
    hpccWasm.Graphviz.load().then(graphviz => {	        	
		const svg = graphviz.dot(dot);
		const div = document.getElementById(pageName + "-graphviz");
		div.innerHTML = graphviz.layout(dot, "svg", "dot");
	});
};