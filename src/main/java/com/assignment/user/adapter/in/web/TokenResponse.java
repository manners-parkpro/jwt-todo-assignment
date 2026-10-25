package com.assignment.user.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonProperty;

record TokenResponse(@JsonProperty("access_token") String accessToken) {
}
