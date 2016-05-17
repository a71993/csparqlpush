var ws = new WebSocket("ws://localhost:8080/echo");

ws.onopen = function() {
	$('#rdf_stream').append("<p>New connection opened!</p>");
};

ws.onmessage = function (evt) {
	var triple = JSON.parse(evt.data);
//	console.log(evt.data);
	$("#results tbody").append(
			"<tr>"+
			"<td>" + triple.subject + "</td>"+
			"<td>" + triple.predicate + "</td>"+
			"<td>" + triple.object + "</td>"+
			"</tr>");
};

ws.onclose = function() {
	$('#rdf_stream').append("<p>Connection closed!</p>");
};

ws.onerror = function(err) {
	$('#rdf_stream').append("<p>Error: "+ err.data + "</p>");
};