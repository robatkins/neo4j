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
package org.neo4j.commandline.admin.security;

import org.junit.Before;
import org.junit.Test;

import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Stream.concat;
import static org.neo4j.server.security.enterprise.auth.plugin.api.PredefinedRoles.ADMIN;
import static org.neo4j.server.security.enterprise.auth.plugin.api.PredefinedRoles.ARCHITECT;
import static org.neo4j.server.security.enterprise.auth.plugin.api.PredefinedRoles.PUBLISHER;
import static org.neo4j.server.security.enterprise.auth.plugin.api.PredefinedRoles.READER;

public class RolesCommandIT extends RolesCommandTestBase
{
    @Before
    public void setUp() throws Throwable
    {
        super.setup();
        // the following line ensures that the test setup code (like creating test users) works on the same initial
        // environment that the actual tested commands will encounter. In particular some auth state is created
        // on demand in both the RolesCommand and in the real server. We want that state created before the tests
        // are run.
        tool.execute( graphDir.toPath(), confDir.toPath(), makeArgs( "roles", "list" ) );
        resetOutsideWorldMock();
    }

    @Test
    public void shouldGetUsageErrorsWithNoSubCommand() throws Throwable
    {
        // When running 'users' with no subcommand, expect usage errors
        assertFailedRolesCommand( "", new String[0],
                "Missing arguments: expected at least one sub-command as argument",
                "neo4j-admin roles <subcommand> [<roleName>] [<username>]",
                "Runs several possible sub-commands for managing the native roles repository" );
    }

    //
    // Tests for list command
    //

    @Test
    public void shouldListDefaultRoles() throws Throwable
    {
        // Given only default roles

        // When running 'list', expect nothing except default roles
        assertSuccessWithDefaultRoles( "list", args() );
    }

    @Test
    public void shouldListNewRole() throws Throwable
    {
        // Given a new role
        createTestRole( "test_role" );

        // When running 'list', expect default roles as well as new role
        assertSuccessWithDefaultRoles( "list", args(), "test_role" );
    }

    @Test
    public void shouldListSpecifiedRole() throws Throwable
    {
        // Given default roles

        // When running 'list' with filter, expect subset of roles
        assertSuccessfulRolesCommand( "list", args("ad"), "admin", "reader" );
    }

    //
    // Tests for create command
    //

    @Test
    public void shouldGetUsageErrorsWithCreateCommandAndNoArgs() throws Throwable
    {
        // When running 'create' with arguments, expect usage errors
        assertFailedRolesCommand( "create", new String[0],
                "Missing arguments: 'roles create' expects roleName argument",
                "neo4j-admin roles <subcommand> [<roleName>] [<username>]",
                "Runs several possible sub-commands for managing the native roles" );
    }

    @Test
    public void shouldCreateNewRole() throws Throwable
    {
        // Given no previously existing role

        // When running 'create' with correct parameters, expect success
        assertSuccessfulRolesCommand( "create", args("another"), "Created new role 'another'" );

        // And the user requires password change
        assertSuccessWithDefaultRoles( "list", args(), "another" );
    }

    @Test
    public void shouldNotCreateDefaultRole() throws Throwable
    {
        // Given default state

        // When running 'create' with correct parameters, expect error
        assertFailedRolesCommand( "create", args("architect"), "The specified role 'architect' already exists" );
    }

    @Test
    public void shouldNotCreateExistingRole() throws Throwable
    {
        // Given a custom pre-existing role
        createTestRole( "another_role" );

        // When running 'create' with correct parameters, expect correct output
        assertFailedRolesCommand( "create", args("another_role"), "The specified role 'another_role' already exists" );
    }

    //
    // Tests for delete command
    //

    @Test
    public void shouldGetUsageErrorsWithDeleteCommandAndNoArgs() throws Throwable
    {
        assertFailedRolesCommand( "delete", new String[0],
                "Missing arguments: 'roles delete' expects roleName argument",
                "neo4j-admin roles <subcommand> [<roleName>] [<username>]",
                "Runs several possible sub-commands for managing the native roles" );
    }

    @Test
    public void shouldNotDeleteNonexistentRole() throws Throwable
    {
        // Given default state

        // When running 'delete' with correct parameters, expect error
        assertFailedRolesCommand( "delete", args( "another" ), "Role 'another' does not exist" );
    }

    @Test
    public void shouldDeleteCustomRole() throws Throwable
    {
        createTestRole( "test_role" );

        // When running 'delete' with correct parameters, expect success
        assertSuccessfulRolesCommand( "delete", args( "test_role" ), "Deleted role 'test_role'" );
    }

    @Test
    public void shouldNotDeletePredefinedRole()
    {
        // given default test

        assertFailedRolesCommand( "delete", args( "admin" ), "'admin' is a predefined role and can not be deleted" );
    }

    //
    // Tests for assign command
    //

    @Test
    public void shouldGetUsageErrorsWithAssignCommandAndNoArgs() throws Throwable
    {
        assertFailedRolesCommand( "assign", new String[0],
                "Missing arguments: 'roles assign' expects roleName and username arguments",
                "neo4j-admin roles <subcommand> [<roleName>] [<username>]",
                "Runs several possible sub-commands for managing the native roles" );
    }

    @Test
    public void shouldGetUsageErrorsWithAssignCommandAndNoUsername() throws Throwable
    {
        assertFailedRolesCommand( "assign", args("reader"),
                "Missing arguments: 'roles assign' expects roleName and username arguments",
                "neo4j-admin roles <subcommand> [<roleName>] [<username>]",
                "Runs several possible sub-commands for managing the native roles" );
    }

    @Test
    public void shouldNotAssignNonexistentRole() throws Throwable
    {
        // Given default state

        // When running 'assign' with correct parameters, expect error
        assertFailedRolesCommand( "assign", args( "another", "neo4j" ), "Role 'another' does not exist" );
    }

    @Test
    public void shouldNotAssignToNonexistentUser() throws Throwable
    {
        // Given default state

        // When running 'assign' with correct parameters, expect error
        assertFailedRolesCommand( "assign", args( "reader", "another" ), "User 'another' does not exist" );
    }

    @Test
    public void shouldAssignCustomRole() throws Throwable
    {
        createTestRole( "test_role" );
        createTestUser( "another", "abc" );

        // When running 'assign' with correct parameters, expect success
        assertSuccessfulRolesCommand( "assign", args( "test_role", "another" ), "Assigned role 'test_role' to user 'another'" );
    }

    //
    // Tests for remove command
    //

    @Test
    public void shouldGetUsageErrorsWithRemoveCommandAndNoArgs() throws Throwable
    {
        assertFailedRolesCommand( "remove", new String[0],
                "Missing arguments: 'roles remove' expects roleName and username arguments",
                "neo4j-admin roles <subcommand> [<roleName>] [<username>]",
                "Runs several possible sub-commands for managing the native roles" );
    }

    @Test
    public void shouldGetUsageErrorsWithRemoveCommandAndNoUsername() throws Throwable
    {
        assertFailedRolesCommand( "remove", args("reader"),
                "Missing arguments: 'roles remove' expects roleName and username arguments",
                "neo4j-admin roles <subcommand> [<roleName>] [<username>]",
                "Runs several possible sub-commands for managing the native roles" );
    }

    @Test
    public void shouldNotRemoveNonexistentRole() throws Throwable
    {
        // Given default state

        // When running 'remove' with correct parameters, expect error
        assertFailedRolesCommand( "remove", args( "another", "neo4j" ), "Role 'another' does not exist" );
    }

    @Test
    public void shouldNotRemoveFromNonexistentUser() throws Throwable
    {
        // Given default state

        // When running 'remove' with correct parameters, expect error
        assertFailedRolesCommand( "remove", args( "reader", "another" ), "User 'another' does not exist" );
    }

    @Test
    public void shouldAssignAndRemoveCustomRole() throws Throwable
    {
        createTestRole( "test_role" );
        createTestUser( "another", "abc" );

        // When running 'remove' on non-assigned role, expect error
        assertFailedRolesCommand( "remove", args( "test_role", "another" ), "Role 'test_role' was not assigned to user 'another'" );
        // When running 'assign' with correct parameters, expect success
        assertSuccessfulRolesCommand( "assign", args( "test_role", "another" ), "Assigned role 'test_role' to user 'another'" );
        // When running 'assign' on already assigned role, expect error
        assertFailedRolesCommand( "assign", args( "test_role", "another" ), "Role 'test_role' was already assigned to user 'another'" );
        // When running 'remove' with correct parameters, expect success
        assertSuccessfulRolesCommand( "remove", args( "test_role", "another" ), "Removed role 'test_role' from user 'another'" );
        // When running 'assign' on already assigned role, expect error
        assertFailedRolesCommand( "remove", args( "test_role", "another" ), "Role 'test_role' was not assigned to user 'another'" );
    }

    //
    // Tests for 'for' and 'users' commands
    //

    @Test
    public void shouldGetUsageErrorsWithForCommandAndNoArgs() throws Throwable
    {
        assertFailedRolesCommand( "for", new String[0],
                "Missing arguments: 'roles for' expects username argument",
                "neo4j-admin roles <subcommand> [<roleName>] [<username>]",
                "Runs several possible sub-commands for managing the native roles" );
    }

    @Test
    public void shouldGetUsageErrorsWithUsersCommandAndNoArgs() throws Throwable
    {
        assertFailedRolesCommand( "users", new String[0],
                "Missing arguments: 'roles users' expects roleName argument",
                "neo4j-admin roles <subcommand> [<roleName>] [<username>]",
                "Runs several possible sub-commands for managing the native roles" );
    }

    @Test
    public void shouldNotListUsersForNonexistentRole() throws Throwable
    {
        // Given default state

        // When running 'for' with correct parameters, expect error
        assertFailedRolesCommand( "for", args( "another" ), "User 'another' does not exist" );
    }

    @Test
    public void shouldNotListRolesForNonexistentUser() throws Throwable
    {
        // Given default state

        // When running 'users' with correct parameters, expect error
        assertFailedRolesCommand( "users", args( "another" ), "Role 'another' does not exist" );
    }

    @Test
    public void shouldListDefaultRolesAssignments() throws Throwable
    {
        assertSuccessfulRolesCommand( "for", args( "neo4j" ), "admin" );
        assertSuccessfulRolesCommand( "users", args( "admin" ), "neo4j" );
        assertSuccessfulRolesCommand( "users", args( "reader" ) );
        assertSuccessfulRolesCommand( "users", args( "publisher" ) );
        assertSuccessfulRolesCommand( "users", args( "architect" ) );
    }

    @Test
    public void shouldListCustomRoleAssignments() throws Throwable
    {
        createTestRole( "test_role" );
        createTestUser( "another", "abc" );

        // When running 'assign' with correct parameters, expect success
        assertSuccessfulRolesCommand( "assign", args( "test_role", "another" ), "Assigned role 'test_role' to user 'another'" );
        // When running 'for' on already assigned user
        assertSuccessfulRolesCommand( "for", args( "another" ), "test_role" );
        // When running 'for' on already assigned user
        assertSuccessfulRolesCommand( "users", args( "test_role" ), "another" );
    }

    //
    // Utilities for testing AdminTool
    //

    private void assertSuccessWithDefaultRoles( String command, String[] args, String... messages )
    {
        assertSuccessfulRolesCommand( command, args, concat( stream( messages ),
                Stream.of( ADMIN, ARCHITECT, PUBLISHER, READER ) ).toArray( String[]::new ) );
    }

    private void assertSuccessfulRolesCommand( String subCommand, String[] args, String... messages )
    {
        assertSuccessfulSubCommand( "roles", subCommand, args, messages );
    }

    private void assertFailedRolesCommand( String subCommand, String[] args, String... messages )
    {
        assertFailedSubCommand( "roles", subCommand, args, messages );
    }
}
