package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.client.userinfo.github;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.client.userinfo.OAuthUserinfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GithubUserinfo implements OAuthUserinfo, Serializable {
	@JsonAlias("id")
	private String id;
	@JsonAlias("login")
	private String login;
	@JsonAlias("node_id")
	private String nodeId;
	@JsonAlias("name")
	private String name;
	@JsonAlias("email")
	private String email;
	@JsonAlias("avatar_url")
	private String avatarUrl;
	@JsonAlias("gravatar_id")
	private String gravatarId;
	@JsonAlias("url")
	private String url;
	@JsonAlias("htmlUrl")
	private String htmlUrl;
	@JsonAlias("followers_url")
	private String followersUrl;
	@JsonAlias("following_url")
	private String followingUrl;
	@JsonAlias("gists_url")
	private String gistsUrl;
	@JsonAlias("starred_url")
	private String starredUrl;
	@JsonAlias("subscriptions_url")
	private String subscriptionsUrl;
	@JsonAlias("organizations_url")
	private String organizationsUrl;
	@JsonAlias("repos_url")
	private String reposUrl;
	@JsonAlias("events_url")
	private String eventsUrl;
	@JsonAlias("received_events_url")
	private String receivedEventsUrl;
	@JsonAlias("site_admin")
	private String siteAdmin;
	@JsonAlias("company")
	private String company;
	@JsonAlias("blog")
	private String blog;
	@JsonAlias("location")
	private String location;
	@JsonAlias("hireable")
	private Object hireable;
	@JsonAlias("bio")
	private String bio;
	@JsonAlias("twitter_username")
	private String twitterUsername;
	@JsonAlias("public_repos")
	private Long publicRepos;
	@JsonAlias("public_gists")
	private Long publicGists;
	@JsonAlias("followers")
	private Long followers;
	@JsonAlias("following")
	private Long following;
	@JsonAlias("created_at")
	private LocalDateTime createdAt;
	@JsonAlias("updated_at")
	private LocalDateTime updatedAt;
	@JsonAlias("private_gists")
	private Long privateGists;
	@JsonAlias("total_private_repos")
	private Long totalPrivateRepos;
	@JsonAlias("owner_private_repos")
	private Long ownedPrivateRepos;
	@JsonAlias("dist_usage")
	private Long distUsage;
	@JsonAlias("collaborators")
	private Long collaborators;
	@JsonAlias("two_factor_authentication")
	private Boolean twoFactorAuthentication;
	@JsonAlias("plan")
	private Plan plan;

	@Override
	public String subject() {
		return id;
	}

	@Data
	@Accessors(chain = true)

	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Plan {
		@JsonAlias("name")
		private String name;
		@JsonAlias("space")
		private Long space;
		@JsonAlias("collaborators")
		private Long collaborators;
		@JsonAlias("private_repos")
		private Long privateRepos;
	}

}
