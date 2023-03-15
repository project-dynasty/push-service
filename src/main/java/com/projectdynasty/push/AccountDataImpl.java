package com.projectdynasty.push;

import de.alexanderwodarz.code.web.rest.authentication.Authentication;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AccountDataImpl extends Authentication {
    private final AccountData data;
}
