$(document).ready(function() {
	
	console.log("starting results page..");
	
	setInterval('refreshPage()', 5000);
});
    
function refreshPage() { 
	location.reload(); 
	console.log("refreshing..");
}