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
	public static String AtomicIntegerWizard_name;
	
	public static String ConvertToFJTaskRefactoring_check_preconditions;
	public static String ConvertToFJTaskRefactoring_task_name;
	public static String ConvertToFJTaskRefactoring_recursive_method;
	public static String ConvertToFJTaskRefactoring_recursive_action;
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
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, ConcurrencyRefactorings.class);
	}
}