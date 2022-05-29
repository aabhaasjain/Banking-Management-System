<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="ISO-8859-1">
    <title> Admin| Loan View</title>
    <style>
        h3{
            font-family: Calibri;
            font-size: 30pt;
            font-style: normal;
            font-weight: bold;
            color:#D63301;
            text-align: center;
            text-decoration: underline
        }
        body {font-family: Arial, Helvetica, sans-serif;}
        * {box-sizing: border-box;}
    </style>
</head>
<body>

<h3> Nova Bank</h3>
<%
    response.setHeader("Cache-control", "no-cache, no-store, must-revalidate");

    if(session.getAttribute("username")==null||session.getAttribute("role")!="admin")
    {
        response.sendRedirect("/adminlogin");
    }

%>
Welcome, ${sessionScope.username }
<br>

<a href="${pageContext.request.contextPath }/adminlogout">Logout</a>
<br>
<br>
<a href="fetchAllLoanApplication?reqtype=pending">View All Pending Loan Applications</a>
<br>
<a href="fetchAllLoanApplication?reqtype=rejected">View All Rejected Loan Applications</a>
<br>
<a href="fetchAllLoanApplication?reqtype=approved">View All Approved Loan Applications</a>
</body>
</html>