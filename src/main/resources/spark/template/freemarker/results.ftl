<!DOCTYPE html>
<html>
<head>
  
</head>

<body>
<#if image_path??>
<hr>
<div class="container">
<#if start_quota??>
	<h3>Comparision</h3>
	Starting quota: ${start_quota}<br>
	Intrest rate: ${intrest_rate}<br>
</#if>

	<img src=${image_path}>
</div>
</#if>


<#if results??>
<hr>
<div class="container">
	<h3>Database Output</h3>
    <table>
	<tr><th>Date</th><th>Value</th></tr>
    <#list results as x>
      ${x}
    </#list>
    </table>
</div>
</#if>
</body>
</html>
