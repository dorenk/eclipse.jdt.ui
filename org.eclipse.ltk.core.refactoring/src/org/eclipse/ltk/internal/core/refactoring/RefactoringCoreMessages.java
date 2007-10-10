/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring;

import org.eclipse.osgi.util.NLS;

public final class RefactoringCoreMessages extends NLS {

	public static String BufferValidationState_character_encoding_changed;

	public static String BufferValidationState_no_character_encoding;

	private static final String BUNDLE_NAME= "org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages";//$NON-NLS-1$

	public static String Changes_validateEdit;

	public static String CheckConditionContext_error_checker_exists;

	public static String CompositeChange_performingChangesTask_name;

	public static String CreateChangeOperation_unknown_Refactoring;

	public static String DefaultRefactoringDescriptor_cannot_create_refactoring;

	public static String NullChange_name;

	public static String ParticipantDescriptor_correct;

	public static String ParticipantDescriptor_error_class_missing;

	public static String ParticipantDescriptor_error_id_missing;

	public static String ParticipantDescriptor_error_name_missing;

	public static String ParticipantExtensionPoint_participant_removed;

	public static String ParticipantExtensionPoint_wrong_type;

	public static String PerformRefactoringHistoryOperation_perform_refactorings;

	public static String ProcessorBasedRefactoring_check_condition_participant_failed;

	public static String ProcessorBasedRefactoring_create_change;

	public static String ProcessorBasedRefactoring_final_conditions;

	public static String ProcessorBasedRefactoring_initial_conditions;

	public static String Refactoring_execute_label;

	public static String RenameResourceChange_name;

	public static String RenameResourceChange_progress_description;

	public static String RenameResourceDescriptor_error_name_not_defined;

	public static String RenameResourceDescriptor_error_path_not_set;

	public static String RenameResourceDescriptor_error_resource_not_existing;

	public static String RenameResourceDescriptor_unnamed_descriptor;

	public static String RenameResourceProcessor_comment;

	public static String RenameResourceProcessor_description;

	public static String RenameResourceProcessor_error_invalid_name;

	public static String RenameResourceProcessor_error_no_parent;

	public static String RenameResourceProcessor_processor_name;

	public static String RenameResourceProcessor_error_resource_already_exists;

	public static String Refactoring_redo_label;

	public static String Refactoring_undo_label;

	public static String RefactoringCorePlugin_creation_error;

	public static String RefactoringCorePlugin_duplicate_warning;

	public static String RefactoringCorePlugin_internal_error;

	public static String RefactoringCorePlugin_listener_removed;

	public static String RefactoringCorePlugin_missing_attribute;

	public static String RefactoringCorePlugin_missing_class_attribute;

	public static String RefactoringCorePlugin_participant_removed;

	public static String RefactoringHistoryManager_empty_argument;

	public static String RefactoringHistoryManager_error_reading_file;

	public static String RefactoringHistoryManager_non_string_argument;

	public static String RefactoringHistoryManager_non_string_value;

	public static String RefactoringHistoryManager_whitespace_argument_key;

	public static String RefactoringHistoryService_deleting_refactorings;

	public static String RefactoringHistoryService_resolving_information;

	public static String RefactoringHistoryService_retrieving_history;

	public static String RefactoringHistoryService_updating_history;

	public static String RefactoringSessionReader_invalid_contents_at;

	public static String RefactoringSessionReader_invalid_values_in_xml;

	public static String RefactoringSessionReader_missing_version_information;

	public static String RefactoringSessionReader_no_session;

	public static String RefactoringSessionReader_unsupported_version_information;

	public static String RefactoringUndoContext_label;

	public static String Resources_fileModified;

	public static String Resources_modifiedResources;

	public static String Resources_outOfSync;

	public static String ResourceChange_error_no_input;

	public static String ResourceChange_error_read_only;

	public static String ResourceChange_error_does_not_exist;

	public static String ResourceChange_error_has_been_modified;

	public static String ResourceChange_error_read_only_state_changed;

	public static String ResourceChange_error_unsaved;

	public static String Resources_outOfSyncResources;

	public static String TextChanges_error_content_changed;

	public static String TextChanges_error_document_content_changed;

	public static String TextChanges_error_existing;

	public static String TextChanges_error_not_existing;

	public static String TextChanges_error_outOfSync;

	public static String TextChanges_error_read_only;

	public static String TextChanges_error_unsaved_changes;

	public static String UndoableOperation2ChangeAdapter_error_message;

	public static String UndoableOperation2ChangeAdapter_no_redo_available;

	public static String UndoableOperation2ChangeAdapter_no_undo_available;

	public static String UndoManager2_no_change;

	public static String UnknownRefactoringDescriptor_cannot_create_refactoring;

	public static String UnknownRefactoringDescriptor_initialization_error;

	public static String ValidateEditChecker_failed;

	static {
		NLS.initializeMessages(BUNDLE_NAME, RefactoringCoreMessages.class);
	}

	private RefactoringCoreMessages() {
		// Do not instantiate
	}
}
