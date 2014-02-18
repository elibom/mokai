<#ftl encoding="UTF-8">
<#import "layout.ftl" as layout>
<@layout.layout>

	<div class="header-space">
		<form id="filter-messages" class="form-inline" onsubmit="return false;">
			<input id="recipient" type="text" class="input-medium" placeholder="Recipient">
	  		<label class="checkbox">
	    		<input id="processed" type="checkbox"> Processed
	  		</label>
	  		<label class="checkbox">
	    		<input id="failed" type="checkbox"> Failed
	  		</label>
	  		<label class="checkbox">
	    		<input id="unrouted" type="checkbox"> Unrouted
	  		</label>
	  		<button type="submit" class="btn" ><i class="icon-refresh"></i> Apply</button>
		</form>
	</div>

	<div id="messages-table">
		<table class="table table-striped table-bordered table-condensed" style="background: white; margin-bottom: 0px;">
			<thead>
				<tr style="position: relative; top:0px;">
					<th>Id</th>
					<th>Date</th>
					<th>Source</th>
					<th>Destination</th>
					<th>Status</th>
					<th>To</th>
					<th>From</th>
					<th>Sequence</th>
					<th>Message Id</th>
					<th>Cmd Status</th>
					<th>Receipt</th>
					<th>Receipt Date</th>
					<th>Text</th>
				</tr>
			</thead>
			
			<tbody>
				<#list messages as message>
					<tr>
						<td style="min-width: 28px;">${message.id}</td>
						<td style="min-width: 140px;">${message.date}</td>
						<td style="min-width: 70px;">${message.source}</td>
						<td style="min-width: 70px;">${message.destination}</td>
						<td>${message.status}</td>
						<td>${message.to}</td>
						<td>${message.from}</td>
						<td>${message.sequence}</td>
						<td>${message.messageId}</td>
						<#if message.cmdStatus != "0">
							<td class="red">${message.cmdStatus}</td>
						<#else>
							<td>${message.cmdStatus}</td>
						</#if>
						<td style="min-width: 70px;">${message.receipt}</td>
						<td>${message.receiptDate}</td>
						<td style="min-width: 180px;">${message.text}</td>
					</tr>
				</#list>
			</tbody>
		</table>
	</div>
	
	<script type="text/javascript">
	
		function getParameterByName(name) {
			name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
			var regexS = "[\\?&]" + name + "=([^&#]*)";
			var regex = new RegExp(regexS);
			var results = regex.exec(window.location.search);
			if(results == null)
				return "";
			else
				return decodeURIComponent(results[1].replace(/\+/g, " "));
		}
		
		$(document).ready(function() { 
			
			var recipient = getParameterByName('recipient');
			if (recipient.length != 0) {
				$('#recipient').val(recipient);
			}
			
			var status = getParameterByName('status');
			if (status.length == 0) {
				$('#processed').prop("checked", true);
				$('#failed').prop("checked", true);
				$('#unrouted').prop("checked", true);
			} else {
				if (status.indexOf('2') != -1) {
					$('#processed').prop("checked", true);
				}
				
				if (status.indexOf('3') != -1 || status.indexOf('5') != -1) {
					$('#failed').prop("checked", true);
				}
				
				if (status.indexOf('4') != -1) {
					$('#unrouted').prop("checked", true);
				}
			}
		
			
			$('form#filter-messages').submit(function() {
				var recipient = $('#recipient').val();
				var processed = $('#processed').is(':checked');
				var failed = $('#failed').is(':checked');
				var unrouted = $('#unrouted').is(':checked');
				
				var existsCriteria = false;
				var query = "";
				
				if (recipient.length != 0 && recipient.replace(/\s/g, '').length != 0) {
					query = "recipient=" + recipient;
					existsCriteria = true;
				}
				
				var status = "";
				if (!processed || !failed || !unrouted) {
					
					var existsStatus = false;
					if (processed) {
						status = "2";
						existsStatus = true;
					}
					
					if (failed) {
						status += (existsStatus ? "," : "") + "3,5";
						existsStatus = true; 
					}	
					
					if (unrouted) {
						status += (existsStatus ? "," : "") + "4";
						existsStatus = true;
					}
					
					if (status.length != 0) {
						query += (existsStatus ? "&" : "") + "status=" + status;
					}
					
				}
				
				if (query.length != 0) {
					query = "?" + query; 
				}
				
				window.location = window.location.href.split('?')[0] + query;
				
			});
			
		});
		
	</script>
	
</@layout.layout>