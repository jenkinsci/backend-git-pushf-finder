#!/usr/bin/env groovy

// compare what's in GitHub's CSV against the events list

class Tag {
   // before is what we'd like to restore
   // after is what Luca pushed
   String before,after;
   
    static boolean match(s1,s2) {
       s1 = s1.toLowerCase();
       s2 = s2.toLowerCase();
       return s1.startsWith(s2) || s2.startsWith(s1);
    }

   public boolean equals(Object that) {
       return match(this.before,that.before) && match(this.after,that.after);
   }
   
   public String toString() {
       return "${before.substring(0,7)}->${after.substring(0,7)}";
   }
   
   boolean isEmpty() { return before==after; }
}

def fromGitHub = [:]

boolean firstLine = true;
new File("jenkinsci_restore.csv").eachLine { line ->
    if (firstLine) {
        firstLine = false;
        return;
    }
    def tokens = line.split(",")
    fromGitHub[tokens[0]] = new Tag(before:tokens[1], after:tokens[2])
}

new File("events.txt").eachLine { line ->
    if (!line.startsWith("[FOUND"))
        return;
    line = line.substring(10)
    def tokens = line.split(",")
    def repo = tokens[0]
    def t = new Tag(before:tokens[1], after:tokens[2])
    
    def branch = tokens[3];
    
    if (branch=="refs/heads/master") {
        def gh = fromGitHub[repo]
        if (gh==null) {
            println "${repo} not in GitHub"
            return;
        }
        
        if (t!=gh) {
            println "GitHub:${gh}\tKohsuke:${t}\t${repo}"
            if (!Tag.match(t.before,gh.before)) {
                println " ***** ";
            }
        }
    } else {
        println "\t\t\tKohsuke:${t}\t${repo}\tbranch:${branch}"
    }
}
