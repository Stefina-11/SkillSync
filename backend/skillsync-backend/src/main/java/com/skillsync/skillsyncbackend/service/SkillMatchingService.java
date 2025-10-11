package com.skillsync.skillsyncbackend.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SkillMatchingService {

    public MatchResult matchSkills(List<String> resumeSkills, List<String> jobSkills) {
        Set<String> resumeSkillSet = new HashSet<>(resumeSkills);
        Set<String> jobSkillSet = new HashSet<>(jobSkills);

        Set<String> matchedSkills = new HashSet<>(resumeSkillSet);
        matchedSkills.retainAll(jobSkillSet);

        Set<String> missingSkills = new HashSet<>(jobSkillSet);
        missingSkills.removeAll(resumeSkillSet);

        double matchPercentage = 0.0;
        if (!jobSkillSet.isEmpty()) {
            matchPercentage = ((double) matchedSkills.size() / jobSkillSet.size()) * 100;
        }

        return new MatchResult(matchPercentage, new ArrayList<>(missingSkills));
    }

    public static class MatchResult {
        private double matchPercentage;
        private List<String> missingSkills;

        public MatchResult(double matchPercentage, List<String> missingSkills) {
            this.matchPercentage = matchPercentage;
            this.missingSkills = missingSkills;
        }

        public double getMatchPercentage() {
            return matchPercentage;
        }

        public void setMatchPercentage(double matchPercentage) {
            this.matchPercentage = matchPercentage;
        }

        public List<String> getMissingSkills() {
            return missingSkills;
        }

        public void setMissingSkills(List<String> missingSkills) {
            this.missingSkills = missingSkills;
        }
    }
}
