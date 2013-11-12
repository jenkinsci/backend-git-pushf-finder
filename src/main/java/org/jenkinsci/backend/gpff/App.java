package org.jenkinsci.backend.gpff;

import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHEventInfo;
import org.kohsuke.github.GHEventPayload.Push;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.Date;

/**
 * Look for git push -f from Luca across all the repositories.
 */
public class App {
    public static void main(String[] args) throws Exception {
        GHOrganization org = GitHub.connect().getOrganization("jenkinsci");

        OUTER:
        for (GHRepository r : org.listRepositories()) {
            boolean hadEvents = false;

            // give GitHub a break to avoid hitting them too hard
            Thread.sleep(1000);

            for (GHEventInfo e : r.listEvents()) {
                hadEvents = true;

                if (isGitPushF(e)) {
                    Push p = e.getPayload(Push.class);
                    if (p.getSize()==0) {
                        System.out.printf("[FOUND  ] %s,%s,%s,%s\n",
                                r.getName(), p.getBefore(), p.getHead(), p.getRef());
                        // there might be push to multiple refs, so keep looking
                    }
                }
                if (e.getCreatedAt().getTime() < CUT_OFF)
                    continue OUTER;  // we've looked far enough
            }

            // some repositories do not have any updates for a long time, and in those cases
            // the events are empty. those are less of a concern assuming that GitHub won't forget events
            if (hadEvents)
                System.err.println("[WARNING] events don't go back far enough: "+r.getName());
            else
                System.err.println("[INFO   ] no events in "+r.getName());

        }
    }

    /**
     * Looks for a push event from Luca.
     */
    private static boolean isGitPushF(GHEventInfo e) throws IOException {
        return e.getType()==GHEvent.PUSH && e.getActorLogin().equals("lucamilanesio");
    }

    /**
     * Events are sorted by the time, so we can stop the search before going back in time too much.
     *
     * The problematic push happened around 2am GMT Nov 10, so giving it ample 24 hour buffer.
     */
    private static final long CUT_OFF = Date.parse("9 Nov 2013 00:00:00 GMT");
}
