<!DOCTYPE html>
<html>
<head>
  <#include "header.ftl">
</head>

<body>

  <#include "nav.ftl">

 
<div class="container" style="text-align:left;">
<#if !compare??> 
	<form method='post' id='form-db' action='/'> 
		Data początkowa:<br><input type='date' name='start-date' value='2001-01-12'><br>
		Data końcowa:<br><input type='date' name='end-date' value='2002-01-12'><br>
		<input type='submit' value='Wyświetl'>
	</form> 
<#else>
	<form method='post' id='form-compare' action='/compare'> 
		Kwota inwestycji:<br><input type='number' name='start-quota' value='10000'><br>
		Oprocentowanie lokaty:<br><input type='number' name='intrest-rate' value='4'><br>
		
		Data początkowa:<br><input type='date' name='start-date' value='2001-01-12'><br>
		Data końcowa:<br><input type='date' name='end-date' value='2002-01-12'><br>
		<input type='submit' value='Porównaj'>
	</form> 
</#if>
</div>


<#if image_path??>
<hr>
<div class="container">
<#if start_quota??>
	<h3>Porównanie</h3>
	Kwota inwestycji: ${start_quota}<br>
	Oprocentowanie lokaty: ${intrest_rate}%<br>
</#if>

	<img src=${image_path}>
</div>
</#if>


<#if results??>
<hr>
<div class="container">
	<h3>Kurs giełdowy</h3>
    <table>
	<tr><th>Data</th><th>Wartość</th></tr>
    <#list results as x>
      ${x}
    </#list>
    </table>
</div>
</#if>

</body>
</html>
