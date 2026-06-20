package com.flowstudy.core.module.problem.entity;

public class CodeTemplate {

    private Long problemId;
    private String language;
    private String templateCode;
    private String judgeWrapperCode;

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getJudgeWrapperCode() {
        return judgeWrapperCode;
    }

    public void setJudgeWrapperCode(String judgeWrapperCode) {
        this.judgeWrapperCode = judgeWrapperCode;
    }
}
