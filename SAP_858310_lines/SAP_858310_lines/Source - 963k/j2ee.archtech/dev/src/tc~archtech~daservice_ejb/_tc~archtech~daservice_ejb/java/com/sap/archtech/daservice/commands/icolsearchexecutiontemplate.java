package com.sap.archtech.daservice.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

interface IColSearchExecutionTemplate 
{
	public ArrayList<String> parseRequestBody(BufferedReader bodyReader, BufferedWriter responseWriter) throws IOException;
	
	public void respondQueryResults(ArrayList<ResultSet> sqlResults, BufferedWriter responseWriter) throws SQLException, IOException;
}
