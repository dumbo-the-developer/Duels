/*
 * This file is part of Duels, licensed under the MIT License.
 *
 * Copyright (c) Realized
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.realized._duels.dueling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.realized._duels.utilities.Reloadable;
import org.bukkit.entity.Player;

public class RequestManager implements Reloadable {

    private Map<UUID, List<Request>> requests = new HashMap<>();

    private List<Request> getRequests(UUID uuid) {
        List<Request> requests = this.requests.get(uuid);

        if (requests != null) {
            return requests;
        }

        requests = new ArrayList<>();
        this.requests.put(uuid, requests);
        return requests;
    }

    public Result hasRequestFrom(Player sender, Player target) {
        return hasRequestTo(target, sender);
    }

    public Request getRequestFrom(Player sender, Player target) {
        return getRequestTo(target, sender);
    }

    public Result hasRequestTo(Player sender, Player target) {
        Iterator<Request> iterator = getRequests(target.getUniqueId()).iterator();

        while (iterator.hasNext()) {
            Request request = iterator.next();

            if (!request.getSender().equals(sender.getUniqueId())) {
                continue;
            }

            if (request.getTime() + 30000 - System.currentTimeMillis() <= 0) {
                iterator.remove();
                return Result.TIMED_OUT;
            }

            return Result.FOUND;
        }

        return Result.NOT_FOUND;
    }

    public Request getRequestTo(Player sender, Player target) {
        Iterator<Request> iterator = getRequests(target.getUniqueId()).iterator();

        while (iterator.hasNext()) {
            Request request = iterator.next();

            if (!request.getSender().equals(sender.getUniqueId())) {
                continue;
            }

            if (request.getTime() + 30000 - System.currentTimeMillis() <= 0) {
                iterator.remove();
                continue;
            }

            return request;
        }

        return null;
    }

    public void sendRequestTo(Player sender, Player target, Settings settings) {
        getRequests(target.getUniqueId()).add(new Request(sender.getUniqueId(), target.getUniqueId(), settings));
    }

    public void removeRequestFrom(Player sender, Player target) {
        Iterator<Request> iterator = getRequests(sender.getUniqueId()).iterator();

        while (iterator.hasNext()) {
            Request request = iterator.next();

            if (!request.getSender().equals(target.getUniqueId())) {
                continue;
            }

            iterator.remove();
            break;
        }
    }

    @Override
    public void handleReload(ReloadType type) {
        if (type == ReloadType.STRONG) {
            requests.clear();
        }
    }

    public enum Result {

        NOT_FOUND,
        TIMED_OUT,
        FOUND
    }
}
