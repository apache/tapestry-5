function run(d3) {
  var sampleSVG = d3.select("#viz")
        .append("svg")
        .attr("width", 100)
        .attr("height", 100);    

    sampleSVG.append("circle")
        .style("stroke", "gray")
        .style("fill", "white")
        .attr("r", 40)
        .attr("cx", 50)
        .attr("cy", 50)
        .on("mouseover", function(){d3.select(this).style("fill", "aliceblue");})
        .on("mouseout", function(){d3.select(this).style("fill", "white");});
		
}

if (typeof require !== 'undefined') {
	require(["http://cdnjs.cloudflare.com/ajax/libs/d3/7.9.0/d3.js"], function(d3) {
		run(d3);
	})
}
else {
	import("http://cdnjs.cloudflare.com/ajax/libs/d3/7.9.0/d3.js").then(ignored => run(d3));
}