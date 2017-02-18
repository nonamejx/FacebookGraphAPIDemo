package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import facebook4j.Comment;
import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.PagableList;
import facebook4j.Paging;
import facebook4j.Post;
import facebook4j.auth.AccessToken;

// in this code, we'll use Facebook4J to get all emails from the comments of a facebook post
// then we'll save all emails in a text file.
public class MainApp {
	// get your access token at: https://developers.facebook.com/tools/explorer/
	private static String ACCESS_TOKEN = "xxx";
	private static String POST_ID = "xxx";

	public static void main(String[] args) {
		Facebook facebook = new FacebookFactory().getInstance();
		// Use default values for oauth app id.
		facebook.setOAuthAppId("", "");
		facebook.setOAuthAccessToken(new AccessToken(ACCESS_TOKEN, null));
		try {
			Post post = facebook.getPost(POST_ID);
			Set<String> emails = getFullEmails(facebook, post);
			System.out.println("NUmber of Emails: " + emails.size());
			PrintWriter pw = new PrintWriter(new File("emails-" + POST_ID + ".txt"));
			for (String s : emails) {
				pw.println(s);
			}
			pw.close();

		} catch (FacebookException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static Set<String> getEmailFromString(String input) {
		Pattern p = Pattern.compile("\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b", Pattern.CASE_INSENSITIVE);
		Matcher matcher = p.matcher(input);
		Set<String> emails = new HashSet<String>();
		while (matcher.find()) {
			emails.add(matcher.group());
		}
		return emails;
	}

	public static Set<String> getFullEmails(Facebook fb, Post post) {
		List<Comment> fullComments = new ArrayList<>();
		Set<String> fullEmails = new HashSet<String>();
		try {
			// get first few comments using getComments from post
			PagableList<Comment> comments = post.getComments();
			Paging<Comment> paging;
			do {
				for (Comment comment : comments) {
					fullComments.add(comment);
					fullEmails.addAll(getEmailFromString(comment.getMessage()));
				}
				// get next page
				// NOTE: somehow few comments will not be included.
				// however, this won't affect much on our research
				paging = comments.getPaging();
			} while ((paging != null) && ((comments = fb.fetchNext(paging)) != null));

		} catch (FacebookException ex) {
			Logger.getLogger(Facebook.class.getName()).log(Level.SEVERE, null, ex);
		}
		System.out.println("Number of Comments: " + fullComments.size());
		return fullEmails;
	}
}
