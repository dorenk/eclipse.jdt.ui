package org.eclipse.jdt.internal.corext.refactoring.concurrency;

import org.eclipse.osgi.util.NLS;

/**
 * Helper class to get NLSed messages.
 */
public final class ConcurrencyRefactorings extends NLS {

	private static final String BUNDLE_NAME= ConcurrencyRefactorings.class.getName();

	private ConcurrencyRefactorings() {
		// Do not instantiate
	}

	public static String ConcurrencyRefactorings_update_imports;
	public static String ConcurrencyRefactorings_type_error;
	public static String ConcurrencyRefactorings_empty_string;
	public static String ConcurrencyRefactorings_program_name;
	public static String ConcurrencyRefactorings_field_compile_error;
	public static String ConcurrencyRefactorings_read_access;
	public static String ConcurrencyRefactorings_write_access;
	public static String ConcurrencyRefactorings_postfix_access;
	public static String ConcurrencyRefactorings_prefix_access;
	public static String ConcurrencyRefactorings_remove_synch_mod;
	public static String ConcurrencyRefactorings_remove_synch_block;
	public static String ConcurrencyRefactorings_unsafe_op_error_1;
	public static String ConcurrencyRefactorings_unsafe_op_error_2;
	public static String ConcurrencyRefactorings_unsafe_op_error_3;
	public static String ConcurrencyRefactorings_comment;
	public static String ConcurrencyRefactorings_read_and_write_access;

	public static String AtomicIntegerRefactoring_replace_if_statement_with_compare_and_set;
	public static String AtomicIntegerRefactoring_import;
	public static String AtomicIntegerRefactoring_descriptor_description;
	public static String AtomicIntegerRefactoring_field_pattern;
	public static String AtomicIntegerRefactoring_searching_cunits;
	public static String AtomicIntegerRefactoring_precondition_check;
	public static String AtomicIntegerRefactoring_change_type;
	public static String AtomicIntegerRefactoring_atomic_integer;
	public static String AtomicIntegerRefactoring_mapping_error;
	public static String AtomicIntegerRefactoring_compiler_errors;
	public static String AtomicIntegerRefactoring_name;
	public static String AtomicIntegerRefactoring_create_changes;
	public static String AtomicIntegerRefactoring_java_model_exception_rename;
	public static String AtomicIntegerRefactoring_rename_okay;
	public static String AtomicIntegerWizard_name;
	
	public static String ConvertToFJTaskRefactoring_check_preconditions;
	public static String ConvertToFJTaskRefactoring_task_name;
	public static String ConvertToFJTaskRefactoring_recursive_method;
	public static String ConvertToFJTaskRefactoring_recursive_subtype;
	public static String ConvertToFJTaskRefactoring_generate_compute;
	public static String ConvertToFJTaskRefactoring_recursion_error;
	public static String ConvertToFJTaskRefactoring_scenario_error;
	public static String ConvertToFJTaskRefactoring_analyze_error;
	public static String ConvertToFJTaskRefactoring_compile_error;
	public static String ConvertToFJTaskRefactoring_compile_error_update;
	public static String ConvertToFJTaskRefactoring_name_user;
	public static String ConvertToFJTaskRefactoring_create_changes;
	public static String ConvertToFJTaskRefactoring_name_official;
	public static String ConvertToFJTaskRefactoring_sequential_req;
	public static String ConvertToFJTaskRefactoring_descriptor_description;
	public static String ConvertToFJTaskRefactoring_method_pattern;
	public static String ConvertToFJTaskRefactoring_unavailable_operation;
	public static String ConvertToFJTaskRefactoring_parameter_error;
	public static String ConvertToFJTaskRefactoring_comment_warning;
	public static String ConvertToFJTaskRefactoring_method_body_error;
	public static String ConvertToFJTaskRefactoring_statement_error;
	public static String ConvertToFJTaskRefactoring_node_location_error;
	public static String ConvertToFJTaskRefactoring_switch_statement_error;
	public static String ConvertToFJTaskRefactoring_block_error;
	public static String ConvertToFJTaskRefactoring_multiple_block_error;
	public static String ConvertToFJTaskRefactoring_action_name;
	public static String ConvertToFJTaskRefactoring_no_change_error;
	public static String ConvertToFJTaskRefactoring_thrown_exception_error;
	
	public static String Integer_type_signature;
	public static String AtomicInteger_set;
	public static String AtomicInteger_get;
	public static String AtomicInteger_getAndIncrement;
	public static String AtomicInteger_getAndDecrement;
	public static String AtomicInteger_incrementAndGet;
	public static String AtomicInteger_decrementAndGet;
	public static String AtomicInteger_addAndGet;

	public static String AtomicInteger_compareAndSet;
	public static String AtomicInteger_todo_comment_op_cannot_be_executed_atomically;
	public static String AtomicInteger_todo_comment_op_cannot_be_executed_atomically_nl;
	public static String AtomicInteger_todo_comment_statements_not_properly_synchronized;
	public static String AtomicInteger_todo_comment_statements_not_properly_synchronized_block;
	public static String AtomicInteger_todo_comment_statements_not_properly_synchronized_method;
	public static String AtomicInteger_todo_comment_return_statement_could_not_be_executed_atomically;
	public static String AtomicInteger_warning_cannot_remove_synch_mod_return_assignment;
	public static String AtomicInteger_warning_cannot_remove_synch_block_return_assignment;
	public static String AtomicInteger_statement;
	public static String AtomicInteger_warning_cannot_execute_statement_atomically;
	public static String AtomicInteger_warning_cannot_be_refactored_atomically;
	public static String AtomicInteger_unsafe_operator_warning1;
	public static String AtomicInteger_unsafe_operator_warning2;
	public static String AtomicInteger_unsafe_operator_warning3;
	public static String AtomicInteger_unsafe_operator_warning4;
	public static String AtomicInteger_unsafe_operator_warning5;
	public static String AtomicInteger_warning_side_effects1;
	public static String AtomicInteger_warning_side_effects2;
	public static String AtomicInteger_warning_side_effects3;
	public static String AtomicInteger_warning_side_effects4;
	public static String AtomicInteger_warning_two_field_accesses;
	public static String AtomicInteger_warning_two_field_accesses2;
	public static String AtomicInteger_error_side_effects_on_int_field_in_assignment;
	public static String AtomicInteger_error_side_effects_on_int_field_in_assignment2;

	static {
		NLS.initializeMessages(BUNDLE_NAME, ConcurrencyRefactorings.class);
	}
}