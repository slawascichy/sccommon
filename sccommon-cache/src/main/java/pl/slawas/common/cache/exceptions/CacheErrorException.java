/*
 * Copyright (c) 2007-2008 the original author or authors. All rights reserved.
 * 
 * Redistribution and use in source forms is strictly forbidden.
 * 
 * Redistribution and use in binary forms are permitted provided that the
 * following conditions are met:
 * 
 * 1. Redistributions in binary form must reproduce this license file unchanged
 * in the documentation and other materials provided with the distribution.
 * 
 * 2. Web distribution in binary form is strictly forbidden without specific
 * prior written permission from the original author or authors! The original
 * distribution files must not be renamed or modified.
 * 
 * THIS PRODUCT IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package pl.slawas.common.cache.exceptions;

/**
 * 
 * CacheErrorException
 * <p>
 * Klasa informująca o błędzie pamięci podręcznej.
 * </p>
 * 
 * @author Sławomir Cichy &lt;slawas@scisoftware.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class CacheErrorException extends Throwable {

	private static final long serialVersionUID = 1602501396214379967L;

	public CacheErrorException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public CacheErrorException(String arg0) {
		super(arg0);
	}

	public CacheErrorException(Throwable arg0) {
		super(arg0);
	}

}
