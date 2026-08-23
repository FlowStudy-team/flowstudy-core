package com.flowstudy.core.module.ai.entity;

public class AiConversation {
    private Long id;
    private Long userId;
    private Long tutorialId;
    private Long blogId;
    private Long problemId;
    private String title;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getTutorialId() { return tutorialId; }
    public void setTutorialId(Long tutorialId) { this.tutorialId = tutorialId; }
    public Long getBlogId() { return blogId; }
    public void setBlogId(Long blogId) { this.blogId = blogId; }
    public Long getProblemId() { return problemId; }
    public void setProblemId(Long problemId) { this.problemId = problemId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
