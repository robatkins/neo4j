/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.server.rest.security;

import org.junit.Rule;
import org.junit.Test;

import org.neo4j.server.rest.dbms.UserServiceTest;
import org.neo4j.server.security.auth.AuthenticationStrategy;
import org.neo4j.server.security.enterprise.auth.MultiRealmAuthManagerRule;
import org.neo4j.server.security.enterprise.auth.ShiroSubject;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EnterpriseUserServiceTest extends UserServiceTest
{
    @Rule
    public MultiRealmAuthManagerRule authManagerRule = new MultiRealmAuthManagerRule(
            userRepository,
            mock( AuthenticationStrategy.class )
        );

    @Override
    protected void setupAuthManagerAndSubject()
    {
        authManager = authManagerRule.getManager();
        userManager = authManagerRule.getManager().getUserManager();

        ShiroSubject shiroSubject = mock( ShiroSubject.class );
        when( shiroSubject.getPrincipal() ).thenReturn( "neo4j" );
        neo4jSubject = authManagerRule.makeSubject( shiroSubject );
    }

    @Test
    public void shouldLogPasswordChange() throws Exception
    {
        shouldChangePasswordAndReturnSuccess();

        authManagerRule.assertExactlyInfoInLog( "[neo4j]: changed password%s", "" );
    }

    @Test
    public void shouldLogFailedPasswordChange() throws Exception
    {
        shouldReturn422IfPasswordIdentical();

        authManagerRule.assertExactlyErrorInLog( "[neo4j]: tried to change password: %s",
                "Old password and new password cannot be the same." );
    }
}
