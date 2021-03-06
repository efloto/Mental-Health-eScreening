<div id="headerDiv"  class="top_header">
	<div class="container bg_transparent">
		<div class="row">
			<div class="col-md-9">
				<div id="vaLogoDiv">
					<a href="../home"><img src="<c:url value="/resources/images/dva_eha_logo.png" />" alt="Department of Veterans Affairs | eScreening Program" border="0"></a> 
				</div>
			</div>
				
			<div class="col-md-3 text-right">
				<div id="welcomeDiv">
					<div id="loggedInUser"> 
						<sec:authorize access="isAuthenticated()">
							<span id="welcomeMessage" ><span class="glyphicon glyphicon-user"></span> Welcome <sec:authentication property="principal.fullName"/> | </span> 
							<!--   <input  class="buttonSignout" type="button" value="Sign Out">-->
							<a href="handleLogoutRequest">Logout <span class="glyphicon glyphicon-log-out"></span></a>
						</sec:authorize>
						<sec:authorize access="isAnonymous()">
							<div class="header_support">
								<div>Support and Problems</div>
								<span class="label label-primary"><a href="#">Email US</a> OR (800) 827-1000</span>
							</div>						
						</sec:authorize>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>